package thut.pokecubedatabase.serebii;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import thut.pokecubedatabase.Main;

public class ButtonHandler implements ActionListener
{
    SerebiiChecker serebii;
    public ButtonHandler(SerebiiChecker checker)
    {
        this.serebii = checker;
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == serebii.update)
        {
            if (serebii.mode.getActionCommand().equals("All"))
            {
                for (int i = 722; i <= SerebiiChecker.TOTALCOUNT; i++)
                    serebii.updatePokedexEntryFromSerebii(i);
            }
            else
            {
                int num = Integer.parseInt(Main.instance.number.getText());
                serebii.updatePokedexEntryFromSerebii(num);
            }
            return;
        }
        else if(evt.getSource() == serebii.mode)
        {
            if (serebii.mode.getActionCommand().equals("Selected"))
            {
                serebii.mode.setActionCommand("All");
                serebii.mode.setLabel("All");
            }
            else
            {
                serebii.mode.setActionCommand("Selected");
                serebii.mode.setLabel("Selected");
            }
        }
    }

}
