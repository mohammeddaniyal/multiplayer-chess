package io.github.mohammeddaniyal.chess.server.validators;
import javax.swing.*;
public class QueenMoveValidator
{
private QueenMoveValidator(){};
public static byte validateMove(byte startRowIndex,byte startColumnIndex,byte destinationRowIndex,byte destinationColumnIndex,byte[][] board)
{
//for vertical and horizontal movement starts here
byte tile;
if(startColumnIndex==destinationColumnIndex)//vertical movement
{
if(startRowIndex<destinationRowIndex)	
{
for(byte e=(byte)(startRowIndex+1);e<destinationRowIndex;e++)
{
tile=board[e][startColumnIndex];
if(tile!=0) return 0; // tile not empty
}
}else
{
for(byte e=(byte)(startRowIndex-1);e>destinationRowIndex;e--)
{
tile=board[e][startColumnIndex];
if(tile!=0) return 0; // tile not empty
}
}
}else if(startRowIndex==destinationRowIndex)//horizontal movement
{
if(startColumnIndex<destinationColumnIndex)
{
for(byte f=(byte)(startColumnIndex+1);f<destinationColumnIndex;f++)
{
tile=board[startRowIndex][f];
if(tile!=0) return 0;// tile not empty
}
}else
{
for(byte f=(byte)(startColumnIndex-1);f>destinationColumnIndex;f--)
{
tile=board[startRowIndex][f];
if(tile!=0) return 0;// tile not empty
}
}
}
//for vertical and horizontal movement ends here
//for diagonal movement starts here
else
{
byte d1=(byte)(startRowIndex-destinationRowIndex);
byte d2=(byte)(startColumnIndex-destinationColumnIndex);
if(d1<0) d1=(byte)(d1*(-1));
if(d2<0) d2=(byte)(d2*(-1));
if(d1!=d2)
{
return 0;
}
//validating path blocker

//path blocker for top-left
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
}
//for diagonal movement ends here
//movement validation part for Queen ends here
return 1;
}
}
