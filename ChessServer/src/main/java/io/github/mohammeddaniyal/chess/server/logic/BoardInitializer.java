package io.github.mohammeddaniyal.chess.server.logic;
public class BoardInitializer
{
public static byte[][] initializeBoard()
{
return new byte[][]{
{-4,-2,-3,-5,-6,-3,-2,-4},// Black Pieces (Rook,Knight,Bishop,Queen,King,Bishop,Knight,Rook)
{-1,-1,-1,-1,-1,-1,-1,-1},// Black Pawns
{0,0,0,0,0,0,0,0},//Empty row
{0,0,0,0,0,0,0,0},//Empty row
{0,0,0,0,0,0,0,0},//Empty row
{0,0,0,0,0,0,0,0},//Empty row
{1,1,1,1,1,1,1,1},// White Pawns
{4,2,3,5,6,3,2,4}// White Pieces (Rook,Knight,Bishop,Queen,King,Bishop,Knight,Rook)
};
}
}