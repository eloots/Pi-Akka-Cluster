/**
  * Copyright © 2016-2019 Lightbend, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
  * THIS SOFTWARE BY LIGHTBEND, Inc.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package akka_oled

import java.lang.management.ManagementFactory
import java.text.DecimalFormat

import com.sun.management.OperatingSystemMXBean
import org.apache.commons.io.FileUtils

import scala.collection.mutable

trait JavaMetricsScreen {
   def getJavaMetrics(): Array[Array[String]] = {
      val bean = ManagementFactory.getPlatformMXBean(classOf[OperatingSystemMXBean])
      val formatter = new DecimalFormat("#0.00")
      val map = mutable.LinkedHashMap[String, String](
         "Max mem:" -> FileUtils.byteCountToDisplaySize( ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getMax),
         "Curr mem:" -> FileUtils.byteCountToDisplaySize(ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getUsed),
         "CPU:" -> (formatter.format(bean.getSystemCpuLoad) + "%"),
         "Threads:" -> ManagementFactory.getThreadMXBean.getThreadCount.toString,
         "Classes:" -> ManagementFactory.getClassLoadingMXBean.getLoadedClassCount.toString)
      map.toArray.map(x => Array(x._1, x._2))
   }
}
