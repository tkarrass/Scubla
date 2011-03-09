package de.bitsetter.scubla

/*
 * Created by IntelliJ IDEA.
 * User: uditaren
 * Date: 06.03.11
 * Time: 20:47
 */

import de.bitsetter.scubla.ui._

object Main {

  var frmMain:ScublaForm = _

  def main(args: Array[String]) = {
    try {
      var sHost: String = args(0)
      var sName: String = args(1)

      frmMain = new ScublaForm()
      frmMain.getGameview.connect(sHost, sName)

    println(sHost + " " + sName )
    } catch {
      case e =>
        println(e)
    }
  }



}