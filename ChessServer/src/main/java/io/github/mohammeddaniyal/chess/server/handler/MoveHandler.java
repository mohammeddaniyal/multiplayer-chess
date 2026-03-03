package io.github.mohammeddaniyal.chess.server.handler;
import io.github.mohammeddaniyal.chess.server.models.*;
import io.github.mohammeddaniyal.chess.server.logic.*;
import io.github.mohammeddaniyal.chess.common.Move;
import io.github.mohammeddaniyal.chess.common.MoveResponse;
public class MoveHandler
{
private static void updateCastlingStatus(KingCastling kingCastling,Game game,byte piece,byte fromX,byte fromY)
{
if(piece==6 || piece==-6)//king
{
kingCastling.checkCastling=false;
kingCastling.kingMoved=true;
}
else if(piece==4 || piece==-4)//rook
{
if((fromX==7 && fromY==0) || (fromX==0 && fromY==0))
{
kingCastling.leftRookMoved=true;
}
else if((fromX==7 && fromY==7) || (fromX==0 && fromY==7))
{
kingCastling.rightRookMoved=true;
}
}

if(kingCastling.leftRookMoved==true && kingCastling.rightRookMoved==true)
{
kingCastling.checkCastling=false;


}


}
public static byte[][] getPossibleMoves(Game game,byte fromX,byte fromY)
{
KingCastling kingCastling;
if(game.board[fromX][fromY]>0)//white king
{

kingCastling=game.whiteKingCastling;
}else
{

kingCastling=game.blackKingCastling;
}

KingCastling k=new KingCastling();
k.checkCastling=kingCastling.checkCastling;
k.kingMoved=kingCastling.kingMoved;
k.leftRookMoved=kingCastling.leftRookMoved;
k.rightRookMoved=kingCastling.rightRookMoved;
			//passing zero 	means no need to check for stalemate part
byte [][]possibleMoves=CheckmateDetector.getPossibleMoves(game.board,fromX,fromY,k,(byte)0);
game.possibleMoves=possibleMoves;
return possibleMoves;
}
public static MoveResponse validateMove(Game game,Move move)
{

MoveResponse moveResponse=new MoveResponse();
byte fromX=move.fromX;
byte fromY=move.fromY;
byte toX=move.toX;
byte toY=move.toY;
byte validMove=game.possibleMoves[toX][toY];

if(validMove==0) 
{
moveResponse.isValid=0;
moveResponse.castlingType=0;
moveResponse.pawnPromotionTo=0;
moveResponse.ambiguityType=0;
return moveResponse;
}

moveResponse.isValid=1;
moveResponse.castlingType=0;
moveResponse. pawnPromotionTo=0;
moveResponse.ambiguityType=0;

KingCastling kingCastling;
if(game.board[fromX][fromY]>0)//white king
{
kingCastling=game.whiteKingCastling;
}else
{
kingCastling=game.blackKingCastling;
}

//if move is valid update the current board state
byte sourcePiece=game.board[fromX][fromY];

//check for pawn promotion
if(sourcePiece==1 && toX==0) //white pawn
{
//now change the value of sourcePiece
if(move.pawnPromotionTo==0)
{
moveResponse.isValid=0;
return moveResponse;
}
sourcePiece=move.pawnPromotionTo;
moveResponse.pawnPromotionTo=move.pawnPromotionTo;
}else if(sourcePiece==-1 && toX==7)//black pawn
{
if(move.pawnPromotionTo==0)
{
moveResponse.isValid=0;
return moveResponse;
}
sourcePiece=move. pawnPromotionTo;
moveResponse. pawnPromotionTo=move. pawnPromotionTo;
}
game.board[fromX][fromY]=0;
game.board[toX][toY]=sourcePiece;

if(sourcePiece==1 || sourcePiece==-1) isPawnMoveAmbiguous(move,game.board);
//now check for is the current move is ambigious
//for(Knight,Rook and Bishop)
else isMoveAmbiguous(move,game.board);

//update the move response
moveResponse.ambiguityType=move.ambiguityType;


//now check if the move was castling
if(move.castlingType==0)//no castling happened
{
//do nothing
}else if(kingCastling.checkCastling)//checking if castling is possible or not
{
kingCastling.checkCastling=false;
kingCastling.kingMoved=true;
if(move.castlingType==1)//white king side castling 
{
fromX=7;
fromY=7;
toX=7;
toY=5;
moveResponse.castlingType=1;
kingCastling.rightRookMoved=true;
}else if(move.castlingType==2)//white queen side castling
{
fromX=7;
toX=7;
fromY=0;
toY=3;
moveResponse.castlingType=2;
kingCastling.leftRookMoved=true;
}else if(move.castlingType==3)//black king side castling
{
fromX=0;
toX=0;
fromY=7;
toY=5;
moveResponse.castlingType=3;
kingCastling.rightRookMoved=true;
}else if(move.castlingType==4)//black queen side castling
{
fromX=0;
toX=0;
fromY=0;
toY=3;
moveResponse.castlingType=4;
kingCastling.leftRookMoved=true;
}//now update the board state
move.castlingType=moveResponse.castlingType;
sourcePiece=game.board[fromX][fromY];
game.board[fromX][fromY]=0;
game.board[toX][toY]=sourcePiece;
return moveResponse;
}



//in case of castling no need to check this because we know that king and either rook is moved
//so i handled that on castlingCheck 
//after completion of move track whether castling can still be allowed or not 
//to track whether king or either rook moved or not
if(kingCastling.checkCastling)//only when castling is possible
{
//remove game after testing

updateCastlingStatus(kingCastling,game,sourcePiece,fromX,fromY);
}


return moveResponse;
}

public static void isMoveAmbiguous(Move move,byte[][] board)
{
byte sourcePiece=move.piece;
sourcePiece=(byte)((sourcePiece<0)?sourcePiece*-1:sourcePiece);
if(sourcePiece==5 || sourcePiece==6) return; // no need to check ambiguity
sourcePiece=move.piece;
//find the same piece in board and store it's index
byte samePieceX,samePieceY;
samePieceX=samePieceY=-1;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
//skip the sourcePiece
if(e==move.fromX && f==move.fromY) continue;

if(sourcePiece==board[e][f])
{
samePieceX=e;
samePieceY=f;
break;
}
}
//no need to check ambguity when there's no same piece
if(samePieceX==-1) return;

//now check whether can this identical piece can go to that same position
byte canGoToSamePosition=CheckmateDetector.isMoveValid(board,samePieceX,samePieceY,move.toX,move.toY);
if(canGoToSamePosition==0) return;

// now time to identify ambiguity type

//check for file ambguity
if(samePieceY==move.fromY) move.ambiguityType=1;
else move.ambiguityType=2;// rank ambguity case

}

}


public static void isPawnMoveAmbiguous(Move move,byte[][] board)
{
//means only check ambiguity in en passant case
if(board[move.toX][move.toY]==0) return;

byte sourcePiece=move.piece;
if(sourcePiece!=1 || sourcePiece!=-1) return; // no need to check ambiguity
//find the same piece in board and store it's index

byte []sameFile=board[move.fromX];

for(byte fromY=0;fromY<8;fromY++)
{
//skip sourcePiece
if(move.fromY==fromY) continue;
if(sameFile[fromY]==sourcePiece)
{
//check whether it can move or not 
byte canGoToSamePosition=CheckmateDetector.isMoveValid(board,move.fromX,fromY,move.toX,move.toY);
if(canGoToSamePosition==1)
{
//file ambiguity case
move.ambiguityType=1;
return;
}
}
}
//no ambiguity if loops ends 
}




public static byte detectCheckmate(Game game)
{
byte opponent=(byte)((game.activePlayer==1)?0:1);
boolean isCheckmate=CheckmateDetector.detectCheckmate(game.board,opponent);
return (byte)(isCheckmate?1:0);
}

public static byte detectCheck(Game game,Move move)
{
byte opponent=(byte)((game.activePlayer==1)?0:1);
byte isInCheck=CheckmateDetector.detectCheck(game.board,opponent,move.piece,move.toX,move.toY);
return isInCheck;
}


}
