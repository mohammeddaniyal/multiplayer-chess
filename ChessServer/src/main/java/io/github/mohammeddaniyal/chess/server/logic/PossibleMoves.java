package io.github.mohammeddaniyal.chess.server.logic;
import io.github.mohammeddaniyal.chess.server.validators.*;
import io.github.mohammeddaniyal.chess.server.models.KingCastling;
import javax.swing.*;
public class PossibleMoves
{
public static byte[][] getPossibleMoves(byte [][]board,byte startRowIndex,byte startColumnIndex,KingCastling kingCastling)
{
byte [][]possibleMoves=new byte[8][8];
byte sourcePiece=board[startRowIndex][startColumnIndex];
byte targetTile;
byte destinationRowIndex,destinationColumnIndex;
boolean pawn=false;
if(sourcePiece==1 || sourcePiece==-1)// either white pawn or black pawn
{
pawn=true;
} 
for(byte e=0;e<8;e++)
{
destinationRowIndex=e;
for(byte f=0;f<8;f++)
{
destinationColumnIndex=f;
targetTile=board[destinationRowIndex][destinationColumnIndex];
String targetIconPieceColor="";
boolean capture=false;
if(targetTile!=0)
{
capture=true;
}
//capturing of same color piece
if(capture && ( (sourcePiece>0 && targetTile>0) || (sourcePiece<0 && targetTile<0) ) )
{
possibleMoves[destinationRowIndex][destinationColumnIndex]=0;
continue;
}
if(sourcePiece==6 || sourcePiece==-6)
{
possibleMoves[destinationRowIndex][destinationColumnIndex]=KingMoveValidator.validateMove(board,startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,kingCastling);
//king validation part ends here
}else
if(sourcePiece==5 || sourcePiece==-5)
{
possibleMoves[destinationRowIndex][destinationColumnIndex]=QueenMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
}else
if(sourcePiece==2 || sourcePiece==-2)
{
possibleMoves[destinationRowIndex][destinationColumnIndex]=KnightMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex);
}else
if(sourcePiece==3 || sourcePiece==-3)
{
possibleMoves[destinationRowIndex][destinationColumnIndex]=BishopMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
//Bishop validation ends here
}else
if(sourcePiece==4 || sourcePiece==-4)
{
possibleMoves[destinationRowIndex][destinationColumnIndex]=RookMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
}else 
if(sourcePiece==1 || sourcePiece==-1)
{
possibleMoves[destinationRowIndex][destinationColumnIndex]=PawnMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
}
}//inner loop ends
}//outer loop ends
return possibleMoves;
}
}
