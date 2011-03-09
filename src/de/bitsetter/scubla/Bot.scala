/*
 * Created by IntelliJ IDEA.
 * User: uditaren
 * Date: 07.03.11
 * Time: 05:31
 */
package de.bitsetter.scubla

import de.tdng2011.game.library.{ScoreBoard, World, Player}
import de.tdng2011.game.library.util.Vec2
import scala.math._
import de.bitsetter.scubla._

class Bot(vsHost: String, vsName: String, vsShoot: Boolean) {

  private var mcServerConnection:ScubyConnection = new ScubyConnection(vsHost, vsName, processWorld, processScore, processNames)
  protected var mcPlayer:Player = _

  private var mcTarget:Vec2 = null;
  private var mcDirection:Float = 0.0f;

  private def processWorld(vcWorld: World) : Unit = synchronized {
    for (p:Player <- vcWorld.players)
      if (p.publicId == mcServerConnection.getPublicId) {
        mcPlayer = p
        act
        return Nil
      }
  }

  private def processScore(vcScore: Map[Long, Int]) = synchronized {
    if (vcScore.contains(mcServerConnection.getPublicId)) {
      if (vcScore(mcServerConnection.getPublicId) < -20) {
        // Forces connection reset
        mcServerConnection.getConnection.close;
      }
    }
  }

  private def processNames(vcNames: Map[Long, String]) : Unit = synchronized { Nil }

  def act {
    if (mcTarget != null) {
      mcDirection = getShortestDirectionTo(mcTarget)
    }

    val cDir = mcPlayer.direction * 180.0 / Pi.floatValue
    val ab = (mcDirection-cDir+360)%360
    val ba = (cDir-mcDirection+360)%360
    val move = getDistanceTo(mcTarget) > Const.Distance
    if (ab < ba && ab > Const.Hysterese) {
      mcServerConnection.action(false, true, move, vsShoot)
//      mcServerConnection.action(false, false, move, false)
    } else if (ba > Const.Hysterese) {
      mcServerConnection.action(true, false, move, vsShoot)
//      mcServerConnection.action(false, false, move, false)
    } else {
      mcServerConnection.action(false, false, move, vsShoot)
    }

  }

  private def getDistanceTo(vcPos:Vec2) : Float = vcPos match {
    case null => 0.0f
    case _ => (mcPlayer.pos - vcPos).length.floatValue
  }



  private def getShortestDirectionTo(vcPos:Vec2) : Float = {
     var vPos1:Vec2 = vcPos
     var vPos2:Vec2 = vcPos
     var vPos3:Vec2 = vcPos
     var vPos4:Vec2 = vcPos
     if (vPos1.x < 500) vPos1 += new Vec2(1000, 0) else vPos1 += new Vec2(-1000, 0)
     if (vPos2.y < 500) vPos2 += new Vec2(0, 1000) else vPos2 += new Vec2(0, -1000)
     if (vPos3.x < 500) vPos3 += new Vec2(1000, 0) else vPos3 += new Vec2(-1000, 0)
     if (vPos3.y < 500) vPos3 += new Vec2(0, 1000) else vPos3 += new Vec2(0, -1000)
     var dist1 = (mcPlayer.pos - vPos1).length
     var dist2 = (mcPlayer.pos - vPos2).length
     var dist3 = (mcPlayer.pos - vPos3).length
     var dist4 = (mcPlayer.pos - vPos4).length
     var pd = dist1
     var ppos:Vec2 = vPos1
     if (dist2< pd) { pd = dist2; ppos = vPos2 }
     if (dist3< pd) { pd = dist3; ppos = vPos3 }
     if (dist4< pd) { pd = dist4; ppos = vPos4 }
     getDirectionTo(ppos)
  }

  private def getDirectionTo(vcPos:Vec2) : Float = {
    val dx = mcPlayer.pos.x - vcPos.x
    val dy = mcPlayer.pos.y - vcPos.y
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
    a.floatValue
  }

  def fireAt(vcPos: Vec2) {
    mcDirection = getShortestDirectionTo(vcPos)
  }

  def fireAt(vcBot: Bot) {
    fireAt(vcBot.mcPlayer.pos)
  }

  def goto(vcPos: Vec2) {
    mcTarget = vcPos
  }

}
