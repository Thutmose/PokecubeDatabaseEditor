package thut.pokecubedatabase.serebii;

import pokecube.core.database.moves.json.JsonMoves;
import thut.pokecubedatabase.Main;

public class MoveEntry
{
    JsonMoves.MoveJsonEntry entry;

    public MoveEntry(String name)
    {
        entry = JsonMoves.getMoves(Main.movesFile).getEntry(name, true);
    }

}
