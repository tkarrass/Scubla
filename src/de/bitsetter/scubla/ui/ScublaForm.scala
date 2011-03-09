package de.bitsetter.scubla.ui

import javax.swing.JFrame
import java.awt.{Insets, GridBagConstraints, GridBagLayout}

/*
* Created by IntelliJ IDEA.
* User: uditaren
* Date: 06.03.11
* Time: 20:52
*/

class ScublaForm extends JFrame("Scubla") {

  private var mcGameviewPanel: GameviewPanel = new GameviewPanel()


  def getGameview = mcGameviewPanel

  ///

  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  this.setSize(1000, 800)
  this.getContentPane.setLayout(new GridBagLayout())

  this.getContentPane().add(mcGameviewPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0))


  this.setVisible(true)


}