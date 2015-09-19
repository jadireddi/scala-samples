package concurrency.logging

import scala.concurrent.Future
import scala.concurrent.blocking

// https://www.igvita.com/2012/02/29/work-stealing-and-recursive-partitioning-with-fork-join/

/**
 * Created by serge on 18/09/15.
 *
 * Shows some useful statistics about scheduling ex context / pool
 * like how many threads are in use when it works with "for"/"map"/"flatMap"
 */
object SchedulingPoolStats extends App {

  case class Stat(size:Int, active:Int, running:Int, stealing:Long)

  def secondLongWork(): Future[String] = {
    Future {
      blocking {
        Thread.sleep(1000)
        println("done")
        "X"
      }
    }(Services.schedulingEx)
  }

  implicit val ex = Services.schedulingEx

  println(Services.schedulingPool)

println("------ 1 longWork --- ")

  val result1 = for {
               v1 <- secondLongWork()
  } yield v1

  printStats(1000)

println("------ 2 longWork --- ")

  val result2 = for {
    v1 <- secondLongWork()
    v2 <- secondLongWork()
  } yield v1 + v2

  printStats(2000)


println("------ 3 longWork --- ")

  val result3 = for {
    v1 <- secondLongWork()
    v2 <- secondLongWork()
    v3 <- secondLongWork()
  } yield v1 + v2 + v3

  printStats(3000)


println("------ 4 longWork --- ")

  val result4 = for {
    v1 <- secondLongWork()
    v2 <- secondLongWork()
    v3 <- secondLongWork()
    v4 <- secondLongWork()
  } yield v1 + v2 + v3 + v4

  printStats(4000)

println("------ 5 longWork --- ")

  val result5 = for {
    v1 <- secondLongWork()
    v2 <- secondLongWork()
    v3 <- secondLongWork()
    v4 <- secondLongWork()
    v5 <- secondLongWork()
  } yield v1 + v2 + v3 + v4 + v5

  printStats(5000)

  //

  def printStats(durationMs:Int): Unit = {
    val stats = (0 to durationMs) map { x =>
      Thread.sleep(1)
      Stat (
        size = Services.schedulingPool.getPoolSize,
        active = Services.schedulingPool.getActiveThreadCount,
        running = Services.schedulingPool.getRunningThreadCount,
        stealing = Services.schedulingPool.getStealCount
      )
    }

    val maxSize = stats.map {stat =>
      stat.size
    }.max

    val maxActive = stats.map {stat =>
      stat.active
    }.max

    val maxRunning = stats.map {stat =>
      stat.running
    }.max

    val maxStealing = stats.map {stat =>
      stat.stealing
    }.max

    println("max stat[size,active,running,stealing]: " + Stat(maxSize, maxActive, maxRunning, maxStealing) )
  }

}

// Output: here we can see that 'stealing' and 'size' goes up
/*

------ 1 longWork ---
done
max stat[size,active,running,stealing]: Stat(2,1,1,2)
------ 2 longWork ---
done
done
max stat[size,active,running,stealing]: Stat(3,2,2,6)
------ 3 longWork ---
done
done
done
max stat[size,active,running,stealing]: Stat(3,1,1,10)
------ 4 longWork ---
done
done
done
done
max stat[size,active,running,stealing]: Stat(3,2,1,14)
------ 5 longWork ---
done
done
done
done
done
max stat[size,active,running,stealing]: Stat(3,2,2,19)

*/