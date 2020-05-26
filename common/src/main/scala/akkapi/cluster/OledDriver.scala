package akkapi.cluster

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{Behavior, PostStop}
import akka_oled.{ButtonPushHandlers, Logo}
import akkapi.cluster.OledDriver.ScreenState.ScreenState
import akkapi.cluster.OledDriver.{OledDisplayState, ScreenState, SwitchFromTitleToScreen}
import eroled.{BasicFont, BasicOLEDDriver, DrawingCanvas, TextCanvas}

import scala.concurrent.duration._

object OledDriver {

  sealed trait Command

  case class RegisterView(name: String, orderNum: Int) extends Command

  case class UpdateView(orderNum: Int, content: String) extends Command

  case class SwitchFromTitleToScreen(screen: Int) extends Command

  case class SwitchFromLogoToTitle(screen: Int) extends Command

  object NextScreen extends Command

  object PrevScreen extends Command

  object FirstScreen extends Command

  type ViewName = String
  type ViewContent = String

  object ScreenState extends Enumeration {
    type ScreenState = Value
    val Logo, Title, Content = Value
  }

  case class OledDisplayState(currentScreen: Int,
                              screenState: ScreenState,
                              views: Map[Int, (ViewName, ViewContent)]
                             ) {

    def getCurrentViewContent(): String = views(currentScreen)._2

    def getCurrentViewName(): String = views(currentScreen)._1

    def isContentScreen(): Boolean = screenState == ScreenState.Content

    def getMaxScreensNum(): Int = views.keySet.max

  }

  private def createCanvas(): (TextCanvas, DrawingCanvas) = {
    val driver = new BasicOLEDDriver();
    (new TextCanvas(driver, new BasicFont()), new DrawingCanvas(driver, 0, 0, 256, 64))
  }

  def apply(settings: Settings): Behavior[OledDriver.Command] =
    Behaviors.setup { context =>
      Behaviors.withTimers[Command] { timer =>
        val (text, graphics) = createCanvas()
        new OledDriver(settings, context, timer, text, graphics).running(
          OledDisplayState(
            currentScreen = 0,
            screenState = ScreenState.Logo,
            views = Map.empty[Int, (ViewName, ViewContent)])
        )
      }
    }
}

class OledDriver private(
                         settings: Settings,
                         context: ActorContext[OledDriver.Command],
                         timers: TimerScheduler[OledDriver.Command],
                         text: TextCanvas,
                         drawing: DrawingCanvas) extends Logo with ButtonPushHandlers {
  initButtonPush(context.self)
  renderLogo()
  timers.startSingleTimer(OledDriver.SwitchFromLogoToTitle(0), 2.second)

  def running(state: OledDisplayState
             ): Behavior[OledDriver.Command] = Behaviors.withTimers[OledDriver.Command] {
    timers =>
      Behaviors.receive[OledDriver.Command] {
        (context, message) =>
          message match {
            case OledDriver.RegisterView(name, orderNum) =>
              running(state.copy(views = state.views + (orderNum -> (name -> ""))))
            case OledDriver.UpdateView(orderNum, content) =>
              val (name, _) = state.views(orderNum)
              val newState = state.copy(views = state.views + (orderNum -> (name -> content)));
              if (state.isContentScreen() && state.currentScreen == orderNum)
                renderScreen(newState)
              else
                running(newState)
            case OledDriver.SwitchFromLogoToTitle(screen) =>
              val newState = state.copy(currentScreen = screen, screenState = ScreenState.Title)
              renderTitle(newState)
            case OledDriver.SwitchFromTitleToScreen(screen) =>
              val newState = state.copy(currentScreen = screen, screenState = ScreenState.Content)
              text.clear()
              renderScreen(newState)

            // navigating over screens with button
            case OledDriver.FirstScreen =>
              val newState = state.copy(currentScreen = 0, screenState = ScreenState.Title)
              renderTitle(newState)
            case OledDriver.NextScreen =>
              val nextScreen = if (state.currentScreen + 1 <= state.getMaxScreensNum()) state.currentScreen + 1 else 0
              val newState = state.copy(currentScreen = nextScreen, screenState = ScreenState.Title)
              renderTitle(newState)
            case OledDriver.PrevScreen =>
              val prevScreen = if (state.currentScreen - 1 >= 0) state.currentScreen - 1 else state.getMaxScreensNum
              val newState = state.copy(currentScreen = prevScreen, screenState = ScreenState.Title)
              renderTitle(newState)
          }
      }.receiveSignal {
        case (_, signal) if signal == PostStop =>
          text.clear()
          onStop()
          Behaviors.same
      }
  }

  private def renderTitle(state: OledDisplayState) = {
    text.clear()
    text.drawString(0, 21, "Screen " + state.currentScreen + ": " + state.getCurrentViewName())
    timers.startSingleTimer(SwitchFromTitleToScreen(state.currentScreen), 2.second)
    running(state)
  }

  private def renderLogo() = {
    drawing.clear()
    drawing.drawBwImageCentered(logoWidth, logoHeight, logoBytes)
    drawing.drawScreenBuffer()
  }

  private def renderScreen(state: OledDisplayState) = {
    text.drawMultilineString(state.getCurrentViewContent())
    running(state)
  }


}
