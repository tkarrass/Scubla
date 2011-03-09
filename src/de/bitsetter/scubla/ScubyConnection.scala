/*
 * Created by IntelliJ IDEA.
 * User: uditaren
 * Date: 06.03.11
 * Time: 21:15
 */
package de.bitsetter.scubla;

import de.tdng2011.game.library._
import de.tdng2011.game.library.connection._

class ScubyConnection(vsHost: String,
                      vsName: String,
                      vfWorld: World => Unit,
                      vfScore: Map[Long, Int] => Unit,
                      vfNames: Map[Long, String] => Unit) extends AbstractClient(vsHost, RelationTypes.Player) {

  override def name = vsName

  def processWorld(world : World) : Unit = vfWorld(world)

  override def processScoreBoard(scoreBoard : Map[Long, Int]) : Unit = vfScore(scoreBoard)
  override def processNames(names: Map[Long, String]) : Unit = vfNames(names)

}