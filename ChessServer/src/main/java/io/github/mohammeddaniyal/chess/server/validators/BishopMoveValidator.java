package io.github.mohammeddaniyal.chess.server.validators;
import javax.swing.*;
public class BishopMoveValidator
{
public static byte validateMove(byte startRowIndex,byte startColumnIndex,byte destinationRowIndex,byte destinationColumnIndex,byte [][]board)
{
//restricting vertical and horizontal movement of the (bishop)
if((startRowIndex==destinationRowIndex && startColumnIndex!=destinationColumnIndex) || (startRowIndex!=destinationRowIndex && startColumnIndex==destinationColumnIndex)) return 0; 
//validating movement of bishop diagonally on the same tile
byte d1=(byte) (startRowIndex-destinationRowIndex);
byte d2=(byte) (startColumnIndex-destinationColumnIndex);
if(d1<0) d1=(byte)(d1*(-1));
if(d2<0) d2=(byte)(d2*(-1));
if(d1!=d2)
{
return 0;
}
//validating path blocker
byte tile;
byte e,f;
if(destinationRowIndex<startRowIndex && destinationColumnIndex<startColumnIndex)
{
for(e=(byte)(startRowIndex-1),f=(byte)(startColumnIndex-1);e>destinationRowIndex;e--,f--)
{
tile=board[e][f];
if(tile!=0) 
{
return 0;
}
}
//path blocker for top-left ends here
}else //path blocker for top-right
if(destinationRowIndex<startRowIndex && startColumnIndex<destinationColumnIndex)
{
for(e=(byte)(startRowIndex-1),f=(byte)(startColumnIndex+1);e>destinationRowIndex;e--,f++)
{
tile=board[e][f];
if(tile!=0)
{
return 0;
}
}
//path blocker for top-right ends here
}else//path blocker for bottom-left
if(startRowIndex<destinationRowIndex && destinationColumnIndex<startColumnIndex)
{
for(e=(byte)(startRowIndex+1),f=(byte)(startColumnIndex-1);e<destinationRowIndex;e++,f--)
{
tile=board[e][f];
if(tile!=0) 
{
return 0;
}
}
//path blocker for bottom-left ends here
}else//path blocker for bottom-right
if(startRowIndex<destinationRowIndex && startColumnIndex<destinationColumnIndex)
{
for(e=(byte)(startRowIndex+1),f=(byte)(startColumnIndex+1);e<destinationRowIndex;e++,f++)
{
tile=board[e][f];
if(tile!=0)
{
return 0;
}
}
//path blocker for bottom-right ends here
}
return 1;
}
}
