package eroled

class SmartOLEDTest extends WordSpec with MockitoSugar {
   "SmartOLED" should {
      "draw spreadsheet in columns if less then screen" in {
         val a: SmartOLED = mock[SmartOLED]
         when(a.drawSpreadsheetInColumns(any(classOf[Array[Array[String]]]))).thenCallRealMethod()
         PowerMockito.whenNew(classOf[SmartOLED]).withNoArguments().thenReturn(a)
         a.drawSpreadsheetInColumns(Array(Array("123", "123"), Array("234", "234")))

         verify(a, times(1))
            .drawMultilineString("123                          123\n234                          234\n")
      }

      "draw spreadsheet for many key-values" in {
         val a: SmartOLED = mock[SmartOLED]
         when(a.drawSpreadsheetInColumns(any(classOf[Array[Array[String]]]))).thenCallRealMethod()
         PowerMockito.whenNew(classOf[SmartOLED]).withNoArguments().thenReturn(a)
         a.drawSpreadsheetInColumns(Array(Array("123 ", "123"),
            Array("234", "234"), Array("234", "234"), Array("234", "234"),
            Array("234", "234"), Array("234  ", "234")))

         verify(a, times(1))
            .drawMultilineString("123          123 | 234          234\n234          234 | 234          234\n" +
               "234          234\n234          234\n")
      }


      "draw keyValues" in {
         val a: SmartOLED = mock[SmartOLED]
         when(a.drawKeyValues(any(classOf[Array[Array[String]]]))).thenCallRealMethod()
         PowerMockito.whenNew(classOf[SmartOLED]).withNoArguments().thenReturn(a)
         a.drawKeyValues(Array(Array("123 ", "123"), Array("234", "234"), Array("234", "234")
            , Array("234  ", "234"),
            Array("234", "234"), Array("234", "234")))

         verify(a, times(1))
            .drawMultilineString("123  123 | 234234\n234  234 | 234234\n234  234\n234  234\n")
      }
   }


}
