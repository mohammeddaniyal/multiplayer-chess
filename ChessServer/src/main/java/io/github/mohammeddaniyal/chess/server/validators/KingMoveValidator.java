package io.github.mohammeddaniyal.chess.server.validators;
import io.github.mohammeddaniyal.chess.server.logic.*;
import io.github.mohammeddaniyal.chess.server.models.*;
import javax.swing.*;
import java.util.*;
public class KingMoveValidator
{
private KingMoveValidator(){};
public static byte validateMove(byte[][] board,byte startRowIndex,byte startColumnIndex,byte destinationRowIndex,byte destinationColumnIndex,KingCastling kingCastling)
{
//castling part
if(kingCastling.checkCastling==true)
{
byte kingPiece=board[startRowIndex][startColumnIndex];
if(kingPiece==-6)//black king
{
//king's side castling move arrived
if(startRowIndex==0 && startColumnIndex==4 && destinationRowIndex==0 && destinationColumnIndex==6)
{

if(kingCastling.kingMoved==true || kingCastling.rightRookMoved==true)
{
return 0;
}
//checking if tiles are empty
if(board[0][5]!=0 || board[0][6]!=0 )
{
return 0;
}
//checking is king is in checkmate or not
//one of the rule of castling the king cannot be in check
//passing 0 means color is black
ArrayList<PieceMoves> piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)0,startRowIndex,startColumnIndex,false);
if(piecesMoves.size()!=0) 
{
return 0;
}
// Another rule of castling that the destination tile of king should not be in check
//now to check is tile f8 and g8 are not in any threat

//for tile f8
piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)0,(byte)0,(byte)5,false);
if(piecesMoves.size()!=0)
{
return 0;
}
//for tile g8
piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)0,(byte)0,(byte)6,false);
if(piecesMoves.size()!=0)
{
return 0;
}
return 1;
}// king's side castling ends here

//queen's side castling move arrived
if(startRowIndex==0 && startColumnIndex==4 && destinationRowIndex==0 && destinationColumnIndex==2)
{
if(kingCastling.kingMoved==true || kingCastling.leftRookMoved==true)
{
return 0;
}
//checking if tiles are empty
if(board[0][1]!=0 || board[0][2]!=0 || board[0][3]!=0)
{
return 0;
}
//checking is king is in checkmate or not
ArrayList<PieceMoves> piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)0,startRowIndex,startColumnIndex,false);
if(piecesMoves.size()!=0) 
{
return 0;
}
//now to check is tile c1 and d1 are not in any threat

piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)0,(byte)0,(byte)2,false);
if(piecesMoves.size()!=0)
{
return 0;
}
//for tile g8
piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)0,(byte)0,(byte)3,false);
if(piecesMoves.size()!=0)
{
return 0;
}
return 1;
}// queen's side castling ends here
}//castling of black king part ends here



if(kingPiece==6)// white King
{
//king's side castling move arrived
if(startRowIndex==7 && startColumnIndex==4 && destinationRowIndex==7 && destinationColumnIndex==6)
{

if(kingCastling.kingMoved==true || kingCastling.rightRookMoved==true)
{
return 0;
}
//checking if tiles are empty
if(board[7][5]!=0 || board[7][6]!=0)
{
//if tiles are not empty then return false in terms of byte it's 0
return 0;
}
//checking is king is in checkmate or not
ArrayList<PieceMoves> piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)1,startRowIndex,startColumnIndex,false);
if(piecesMoves.size()!=0) 
{
return 0;
}
//now to check is tile f1 and g1 are not in any threat
//for tile f1
piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)1,(byte)7,(byte)5,false);
if(piecesMoves.size()!=0)
{
return 0;
}
//for tile g1
piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)1,(byte)7,(byte)6,false);
if(piecesMoves.size()!=0)
{
return 0;
}
return 1;
}// king's side castling ends here

//queen's side castling move arrived
if(startRowIndex==7 && startColumnIndex==4 && destinationRowIndex==7 && destinationColumnIndex==2)
{
if(kingCastling.kingMoved==true || kingCastling.leftRookMoved==true)
{
return 0;
}
//checking if tiles are empty
if(board[7][1]!=0 || board[7][2]!=0 || board[7][3]!=0)
{
return 0;
}
//checking is king is in checkmate or not
ArrayList<PieceMoves> piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)1,startRowIndex,startColumnIndex,false);
if(piecesMoves.size()!=0) 
{
return 0;
}
//now to check is tile c1 and d1 are not in any threat

piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)1,(byte)7,(byte)2,false);
if(piecesMoves.size()!=0)
{
return 0;
}
//for tile g8
piecesMoves=CheckmateDetector.isPieceInDanger(board,(byte)1,(byte)7,(byte)3,false);
if(piecesMoves.size()!=0)
{
return 0;
}
return 1;
}// queen's side castling ends here


}//castling of white king part ends here
}//castling part ends here


byte d=0;
if(startColumnIndex==destinationColumnIndex)//moving one step veritcally
{
d=(byte)((startRowIndex<destinationRowIndex)?destinationRowIndex-startRowIndex:startRowIndex-destinationRowIndex);
if(d!=1) return 0;
}else if(startRowIndex==destinationRowIndex)//moving one step horizontally
{
d=(byte)((startColumnIndex<destinationColumnIndex)?destinationColumnIndex-startColumnIndex:startColumnIndex-destinationColumnIndex);
if(d!=1) return 0;
}
else//moving one step diagonally
{
byte d1=(byte)((startRowIndex<destinationRowIndex)?destinationRowIndex-startRowIndex:startRowIndex-destinationRowIndex);
byte d2=(byte)((startColumnIndex<destinationColumnIndex)?destinationColumnIndex-startColumnIndex:startColumnIndex-destinationColumnIndex);
if(d1!=1 || d2!=1) 
{
return 0;
}
}
return 1;
}
}//class ends here (KingValidator)
