import io.github.mohammeddaniyal.chess.server.logic.*;
import io.github.mohammeddaniyal.chess.server.models.*;
class eg2
{
public static void main(String gg[])
{
byte board[][] = {
    {6, 0, 0, 0, 0, 0, 0, 0},
 {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {1, 0, 0, 0, 0, 5, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, -6}
};

Game game=new Game();
game.board=board;
game.whiteKingCastling=new KingCastling();
game.blackKingCastling=new KingCastling();


}
}