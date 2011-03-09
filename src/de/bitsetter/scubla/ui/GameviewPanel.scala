/*
 * Created by IntelliJ IDEA.
 * User: uditaren
 * Date: 06.03.11
 * Time: 21:04
 */
package de.bitsetter.scubla.ui

import javax.swing.JPanel
import java.awt.{Color, Graphics2D, Graphics}
import scala.math._
import de.tdng2011.game.library.{Shot, Player, ScoreBoard, World}
import collection.SortedMap
import de.tdng2011.game.library.util.Vec2
import de.bitsetter.scubla.{Bot, Const, ScubyConnection}

class GameviewPanel extends JPanel {

  private var mcServer:ScubyConnection = _

  private var botList:List[Bot] = List.empty
  private var killList:List[Bot] = List.empty

  def connect(vsHost: String, vsName: String) {
    mcServer = new ScubyConnection(vsHost, vsName, processWorld, processScore, processNames);
    //botList = List(new Bot(vsHost, "D_0", false))
    //for (i:Int <- 1 until 0) {
    //  botList = new Bot(vsHost, "D_" + i, false) :: botList
    //}
    //killList = List(new Bot(vsHost, "K_0", true))
    //for (i:Int <- 1 until 0) {
    //  killList = new Bot(vsHost, "K_" + i, true) :: killList
    //}
    repaint()
  }



  private var players:IndexedSeq[Player] = null
  private var shots:IndexedSeq[Shot] = null

  var pMe:Player = null;
  var ps:Player = null;
  var pd:Double = 1000.0;
  var psx = 0.0f
  var psy = 0.0f
  var ppos:Vec2 = null;

  var oj:Long = 0;
  var shotSpeed = 0;

  private def processWorld(vcWorld: World) : Unit = synchronized {
    players = vcWorld.players;
    shots = vcWorld.shots;
    if (shotSpeed == 0 && shots.size > 0)
      shotSpeed = shots(0).speed


//    for (p:Player <- vcWorld.players)
//      players = List(players, p)
    repaint()

    pMe = null;
    ps = null;
    pd = 1000.0;
    players.foreach { player =>
      if (player.publicId == mcServer.getPublicId) {
        pMe = player
        for (b:Bot <- botList) { b.goto(pMe.pos) }
      } else if (player.publicId == oj) {
        for (b:Bot <- killList) { b.goto(player.pos)}
      }
    }
    players.foreach { player =>
      if (player.publicId != mcServer.getPublicId) {
        //var vPos1:Vec2 = player.pos;
        //var vPos2:Vec2 = player.pos;
        //var vPos3:Vec2 = player.pos;
        var vPos4:Vec2 = player.pos;
        //if (vPos1.x < 500) vPos1 += new Vec2(1000, 0) else vPos1 += new Vec2(-1000, 0)
        //if (vPos2.y < 500) vPos2 += new Vec2(0, 1000) else vPos2 += new Vec2(0, -1000)
        //if (vPos3.x < 500) vPos3 += new Vec2(1000, 0) else vPos3 += new Vec2(-1000, 0)
        //if (vPos3.y < 500) vPos3 += new Vec2(0, 1000) else vPos3 += new Vec2(0, -1000)
        //var dist1 = (pMe.pos - vPos1).length
        //var dist2 = (pMe.pos - vPos2).length
        //var dist3 = (pMe.pos - vPos3).length
        var dist4 = (pMe.pos - vPos4).length
        //if (dist1< pd) { pd = dist1; ps = player; ppos = vPos1 }
        //if (dist2< pd) { pd = dist2; ps = player; ppos = vPos2 }
        //if (dist3< pd) { pd = dist3; ps = player; ppos = vPos3 }
        if (dist4< pd) { pd = dist4; ps = player; ppos = vPos4 }
      }
    }

    def getTargetPos(me:Vec2, meDir:Float, you:Vec2, youDir:Float) : Vec2 = {
      // me zum ursprung machen
      var pos:Vec2 = you - me
      var dir:Vec2 = Vec2(math.cos(youDir), math.sin(youDir))
      var time:Float = pos.length / shotSpeed

      pos = pos + (dir * time * pMe.speed)
    }

    psx = ppos.x // ps.pos.x
    psy = ppos.y // ps.pos.y

    if (ps.thrust) {
      var porig = ps.pos - pMe.pos
      var m1x = math.cos(ps.direction).floatValue
      var m1y = math.sin(ps.direction).floatValue
      var m1 = m1y/m1x

      var m2x = (m1x.floatValue * ps.speed.floatValue) / shotSpeed.floatValue +  (porig.x.floatValue / shotSpeed.floatValue)
      var m2y = (m1y.floatValue * ps.speed.floatValue) / shotSpeed.floatValue +  (porig.y.floatValue / shotSpeed.floatValue)
      var m2 = m2y/m2x
      //var m2y = (porig.y.floatValue + m1y.floatValue * ps.speed.floatValue) / shotSpeed.floatValue

      var Y = porig.y - porig.x * m1

      psx =  Y / (m2 - m1)
      psy =  m1 * psx + Y

      psx += pMe.pos.x
      psy += pMe.pos.y
      //psx += math.cos(ps.direction).floatValue * ps.speed * Const.Speed * (pd * pd / Const.DistFactor).floatValue
      //psy += math.sin(ps.direction).floatValue * ps.speed * Const.Speed * (pd * pd / Const.DistFactor).floatValue
    }

    val dx = pMe.pos.x - psx
    val dy = pMe.pos.y - psy
    var a = 0.0
    if (-0.001 < dx && dx < 0.001) {
      if (dy > 0)
        a = 270
      else
        a = 90
    } else {
      a = ((math.atan(dy/dx) * 180.0 / Pi.floatValue) + 360) % 360
      if (dx > 0)
        a = (a+180) % 360
    }

    val dd = pMe.direction * 180.0 / Pi.floatValue
    val ab = (a-dd+360)%360
    val ba = (dd-a+360)%360
    val move = false //(pd > Const.Distance)
    if (ab < ba && ab > Const.Hysterese) {
      mcServer.action(false, true, move, false)//(ab < Const.Hysterese*3))
//      mcServer.getConnection.getOutputStream.flush
//      mcServer.action(false, false, (pd > Const.Distance), false)
    } else if (ba > Const.Hysterese) {
      mcServer.action(true, false, move, false)//(ba < Const.Hysterese*3))
//      mcServer.getConnection.getOutputStream.flush
//      mcServer.action(false, false, (pd > Const.Distance), false)
    } else {
      mcServer.action(false, false, move, true)
    }
//    mcServer.action(false, false, move, false)
//    mcServer.getConnection.getOutputStream.flush


    Nil
  }


  private var scores:SortedMap[Int, Long] = SortedMap.empty

  private def processScore(vcScore: Map[Long, Int]) : Unit = synchronized {
    scores = SortedMap.empty
    vcScore.foreach { bla =>
      scores = scores + (bla._2 -> bla._1)
      //if (vcScore.)
    }

    repaint()
    Nil
  }

  private def processNames(vcNames: Map[Long, String]) : Unit = {
    if (oj != 0)
      return Nil
    vcNames.foreach { bla =>
      if (bla._2 == "##OJ##") {
        oj = bla._1
      }
    }
    Nil
  }

  override def paint(vcGraphics: Graphics) {
    var g2d:Graphics2D = vcGraphics.create().asInstanceOf[Graphics2D]

    g2d.setColor(Color.black)
    g2d.fillRect(0, 0, this.getWidth(), this.getHeight())
    g2d.setColor(Color.white)

    if (mcServer != null) {
      val factor:Float = math.min(this.getWidth(), this.getHeight()).floatValue / 1000.0f;
      synchronized {
        var i:Int = 1
        scores.foreach { bla =>
          if (bla._2 == mcServer.getPublicId)
            g2d.setColor(Color.cyan)
          else
            g2d.setColor(Color.darkGray)
          g2d.drawString(i + " - " + bla._2 + " (" + bla._1 + ")", 20, this.getHeight - (i * 20) )
          i += 1
        }

        if (players != null)
          players.foreach { bla =>
            val x = (bla.pos.x * factor).intValue
            val y = (bla.pos.y * factor).intValue
            if (bla.publicId == mcServer.getPublicId) {
              g2d.setColor(Color.red)
              g2d.drawOval(x - 10, y - 10, 21, 21)
            } else if (ps != null && ps.publicId == bla.publicId) {
              g2d.setColor(Color.red)
              g2d.fillOval(x - 10, y - 10, 21, 21)
            }
            g2d.setColor(Color.white)
            g2d.drawRect(x-3, y-3, 7, 7)
            g2d.setColor(Color.green)
            g2d.drawLine(x, y, x + (math.cos(bla.direction) * 20).intValue, y + (math.sin(bla.direction) * 20).intValue )
            g2d.setColor(Color.blue)
            g2d.drawString("" + bla.publicId, x, y)
          }
        if (shots != null)
          shots.foreach { bla =>
            val x = (bla.pos.x * factor).intValue
            val y = (bla.pos.y * factor).intValue
            g2d.setColor(Color.red)
            g2d.fillOval(x-3, y-3, 7, 7)
          }
        g2d.setColor(Color.yellow)
        val px = (psx * factor).intValue
        val py = (psy * factor).intValue
        g2d.fillOval(px-3, py-3, 7, 7)
        g2d.drawOval(px-10, py-10, 21,21)
        g2d.drawLine((ps.pos.x * factor).intValue, (ps.pos.y * factor).intValue, px, py)
      }
    } else {
      g2d.drawString("not connected", 20, 20)
    }

    g2d.dispose
  }

}