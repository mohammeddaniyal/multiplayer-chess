package io.github.mohammeddaniyal.chess.server.validators;
import javax.swing.*;
public class PawnMoveValidator
{
public static byte  validateMove(byte startRowIndex,byte startColumnIndex,byte destinationRowIndex,byte destinationColumnIndex,byte [][] board)
{
byte sourcePiece=board[startRowIndex][startColumnIndex];
boolean capture=false;
if(sourcePiece==-1)//blackPawn
{
if(destinationRowIndex<=startRowIndex)
{
// for restricting backward movement of the pawn
return 0;
}

if(startRowIndex==1)//at begining allowing 2 steps
{
if(destinationRowIndex-startRowIndex==2)//2 square tile forward validation
{
//not allowing to jump from a piece if it is in path of the pawn

byte  tile=board[startRowIndex+1][startColumnIndex];
if(tile!=0) return 0;
if(board[startRowIndex+2][startColumnIndex]!=0)return 0;
}
if((destinationRowIndex-startRowIndex!=2 && destinationRowIndex-startRowIndex!=1))
{
//for 2 steps at the first move of the pawn
return 0;
}
//at begining allowing 2 steps ends here
}else if(startRowIndex!=1 && destinationRowIndex-startRowIndex!=1)
{
// restrictring more than one tile movement of the pawn
return 0;
}
if(startRowIndex==destinationRowIndex-1 && startColumnIndex==destinationColumnIndex && board[startRowIndex+1][startColumnIndex]!=0)
{
//restricting movement if any piece is in the forward path
return 0;
}
//capture  part of pawn(en passant)diagonal right or left
if( (startRowIndex+1==destinationRowIndex) && ((startColumnIndex-1==destinationColumnIndex) || (startColumnIndex+1==destinationColumnIndex)) )
{
byte  targetPiece=board[destinationRowIndex][destinationColumnIndex];
if(targetPiece==0) return 0;// no piece to capture by which en passant can be performed
if( (sourcePiece>0 && targetPiece>0) || (sourcePiece<0 && targetPiece<0))
{
//not allowing the capture of same piece color
return 0;
}
capture=true;
}
if(capture!=true && startColumnIndex!=destinationColumnIndex)
{
//restricting pawn diagonal movement other than en passant
return 0;
}
//black pawn part ends here
}else if(sourcePiece==1)
{
if(startRowIndex<=destinationRowIndex)
{
// for restricting backward movement of the pawn
return 0;
}
if(startRowIndex==6)//at begining allowing 2 steps
{
if(startRowIndex-destinationRowIndex==2)//2 square tile forward validation
{
//not allowing to jump from a piece if it is in path of the pawn
byte  tile=board[startRowIndex-1][startColumnIndex];
//checking the path isn't blocked
if(tile!=0) return 0;
if(board[startRowIndex-2][startColumnIndex]!=0)return 0;
}
if(startRowIndex-destinationRowIndex!=2 && startRowIndex-destinationRowIndex!=1)
{
//for 2 steps at the first move of the pawn
return 0;
}
//at begining allowing 2 steps ends here
}else
if(startRowIndex!=6 && startRowIndex-destinationRowIndex!=1)
{
// restrictring more than one tile movement of the pawn
return 0;
}
//path blocking in forward move case of pawn
if(startRowIndex==destinationRowIndex+1 && startColumnIndex==destinationColumnIndex &&  board[startRowIndex-1][startColumnIndex]!=0)
{
return 0;
}
//capture  enpassant
if( (startRowIndex-1==destinationRowIndex) && ((startColumnIndex-1==destinationColumnIndex) || (startColumnIndex+1==destinationColumnIndex)) )
{
byte  targetPiece=board[destinationRowIndex][destinationColumnIndex];
if(targetPiece==0) return 0;
if( (sourcePiece>0 && targetPiece>0) || (sourcePiece<0 && targetPiece<0))
{
//not allowing the capture of same piece color
return 0;
}
capture=true;
}
if(capture!=true && startColumnIndex!=destinationColumnIndex)
{
//not allowing diagonal movement of pawn other than en passant 
return 0;
}

}//white pawn part ends here
return 1;
}
}
 
