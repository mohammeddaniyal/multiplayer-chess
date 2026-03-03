package io.github.mohammeddaniyal.chess.server.validators;
import javax.swing.*;
public class RookMoveValidator
{
public static byte validateMove(byte startRowIndex,byte startColumnIndex,byte destinationRowIndex,byte destinationColumnIndex,byte[][] board)
{
if(startRowIndex!=destinationRowIndex && startColumnIndex!=destinationColumnIndex) return 0;//restricting rook diagonal movement
byte tile;
if(startColumnIndex==destinationColumnIndex)//vertical movement
{
if(startRowIndex<destinationRowIndex)	
{
for(byte e=(byte)(startRowIndex+1);e<destinationRowIndex;e++)
{
tile=board[e][startColumnIndex];
if(tile!=0) return 0;
}
}else
{
for(byte e=(byte)(startRowIndex-1);e>destinationRowIndex;e--)
{
tile=board[e][startColumnIndex];
if(tile!=0) return 0;
}
}
}else if(startRowIndex==destinationRowIndex)//horizontal movement
{
if(startColumnIndex<destinationColumnIndex)
{
for(byte f=(byte)(startColumnIndex+1);f<destinationColumnIndex;f++)
{
tile=board[startRowIndex][f];
if(tile!=0) return 0;
}
}else
{
for(byte f=(byte)(startColumnIndex-1);f>destinationColumnIndex;f--)
{
tile=board[startRowIndex][f];
if(tile!=0) return 0;
}
}
}
return 1;
}
}
