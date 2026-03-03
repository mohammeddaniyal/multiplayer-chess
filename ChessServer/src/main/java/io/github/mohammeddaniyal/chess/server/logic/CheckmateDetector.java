package io.github.mohammeddaniyal.chess.server.logic;
import io.github.mohammeddaniyal.chess.server.validators.*;
import io.github.mohammeddaniyal.chess.server.models.*;
import java.util.*;
public class CheckmateDetector
{
private static byte isValidMove(byte [][]board,byte piece,byte fromX,byte fromY,byte toX,byte toY)
{
byte valid=0;
if(piece==3 || piece==-3)
{
valid= BishopMoveValidator.validateMove(fromX,fromY,toX,toY,board);
}else if(piece==4 || piece==-4)
{
valid=RookMoveValidator.validateMove(fromX,fromY,toX,toY,board);
}else if(piece==2 ||  piece==-2)
{
valid= KnightMoveValidator.validateMove(fromX,fromY,toX,toY);
}else if(piece==1 || piece==-1)
{
valid= PawnMoveValidator.validateMove(fromX,fromY,toX,toY,board);
}else if(piece==5 || piece==-5)
{
valid= QueenMoveValidator.validateMove(fromX,fromY,toX,toY,board);
}

return valid;
}

//method to detect whther the king is in check or not
public static byte detectCheck(byte [][]board,byte opponent,byte attackingPiece,byte attackingPieceX,byte attackingPieceY)
{
List<byte[]> playerPieces=new LinkedList<>();
byte kingPiece=(byte)((opponent==0)?-6:6);
//find the index of king
byte kingX,kingY;
kingX=kingY=-1;
byte []indexes;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if((board[e][f]>0 && opponent!=1) || (board[e][f]<0 && opponent!=0))
{
//creating a list of indexes of player pieces
if(board[e][f]!=(kingPiece*-1))
{
//no need to add player king index
indexes=new byte[2];
indexes[0]=e;
indexes[1]=f;
playerPieces.add(indexes);
}
}
if(board[e][f]==kingPiece){
kingX=e;
kingY=f;
}
}
}
if(kingX==-1) return 0;
//now check whether the current move place the opponent king in danger
byte valid=0;
valid=isValidMove(board,attackingPiece,attackingPieceX,attackingPieceY,kingX,kingY);

if(valid==0)
{
//now check if it puts the king indirectly in check
//now time to iterate the playerPieces list
for(byte []indxs:playerPieces)
{
byte fromX=indxs[0];
byte fromY=indxs[1];
valid=isValidMove(board,board[fromX][fromY],fromX,fromY,kingX,kingY);
if(valid==1) break;
}
}

return valid;
}

public static byte isMoveValid(byte [][]board,byte startRowIndex,byte startColumnIndex,byte destinationRowIndex,byte destinationColumnIndex)
{
byte piece=board[startRowIndex][startColumnIndex];
byte valid=0;
if(piece==3 || piece==-3)
{
valid= BishopMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
}else if(piece==4 || piece==-4)
{
valid=RookMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
}else if(piece==2 ||  piece==-2)
{
valid= KnightMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex);
}else if(piece==1 || piece==-1)
{
valid= PawnMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
}else if(piece==5 || piece==-5)
{
valid= QueenMoveValidator.validateMove(startRowIndex,startColumnIndex,destinationRowIndex,destinationColumnIndex,board);
}
if(valid==0) return 0;
//determine king's index

byte kingRowIndex=0;
byte kingColumnIndex=0;
byte kingPiece=(byte)((piece>0)?6:-6);

for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if(board[e][f]==kingPiece) 
{
kingRowIndex=e;
kingColumnIndex=f;
break;
}
}
}



//create dummyBoard ,without the source piece to simulate or apply no self check rule
byte [][]dummyBoard=new byte[8][8];
byte dummyTile;
byte tile;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
tile=board[e][f];
if(e==startRowIndex && f==startColumnIndex)
{
dummyTile=0;
}
else
{
dummyTile=tile;
}
dummyBoard[e][f]=dummyTile;
}
}//creating dummy tiles(D.S) ends here
byte pieceColor=(byte)((piece>0)?1:0);
ArrayList<PieceMoves> capturingPiecesMovesList;

dummyBoard[destinationRowIndex][destinationColumnIndex]=piece;
capturingPiecesMovesList=isPieceInDanger(dummyBoard,pieceColor,kingRowIndex,kingColumnIndex,false);
byte [][]possibleMoves;

for(PieceMoves pieceMoves:capturingPiecesMovesList)
{
possibleMoves=pieceMoves.possibleMoves;
if(possibleMoves[kingRowIndex][kingColumnIndex]==1)
{
//king is in danger
return 0;
}
}//for loop ends(possibleMovesCapture)
// valid move
return 1;
}

public static byte[][] getPossibleMoves(byte[][] board,byte startRowIndex,byte startColumnIndex,KingCastling kingCastling,byte calledForStalemate)
{
byte [][]possibleMoves=PossibleMoves.getPossibleMoves(board,startRowIndex,startColumnIndex,kingCastling);
PossibleMovesIndex pieceValidIndex;
ArrayList<PossibleMovesIndex> piecesValidIndexes=new ArrayList<>();
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if(possibleMoves[e][f]==1)//can move to this index
{
pieceValidIndex=new PossibleMovesIndex();
pieceValidIndex.row=e;
pieceValidIndex.column=f;
pieceValidIndex.safe=true;
piecesValidIndexes.add(pieceValidIndex);
}
}
}//piece valid indexes loop ends here

//means for checking stalemate condition this piece have no valid moves 
if(calledForStalemate==1 && piecesValidIndexes.size()==0) return null;

if(piecesValidIndexes.size()==0) return possibleMoves;
byte [][]validPossibleMoves=possibleMoves;
//find index of king
byte sourcePiece=board[startRowIndex][startColumnIndex];
//+ve represents white pieces and vice versa 
// 6 represent king
byte kingPiece=(byte)((sourcePiece>0)?6:-6);

byte kingRowIndex=0;
byte kingColumnIndex=0;
boolean pieceIsKing=false;
if(sourcePiece==kingPiece)
//pieceName.equals("whiteKing") || pieceName.equals("blackKing"))
{
kingRowIndex=startRowIndex;
kingColumnIndex=startColumnIndex;
pieceIsKing=true;
}
else
{
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if(board[e][f]==kingPiece) 
{
kingRowIndex=e;
kingColumnIndex=f;
break;
}
}
}
}//else ends

byte [][]dummyBoard=new byte[8][8];
byte dummyTile;
byte tile;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
tile=board[e][f];
if(e==startRowIndex && f==startColumnIndex)
{
dummyTile=0;
}
else
{
dummyTile=tile;
}
dummyBoard[e][f]=dummyTile;
}
}//creating dummy tiles(D.S) ends here

int possibleMovesSize=piecesValidIndexes.size();

byte row;
byte column;
ArrayList<PieceMoves> capturingPiecesMovesList;
byte pieceColor=(byte)((sourcePiece>0)?1:0);
for(PossibleMovesIndex pmi:piecesValidIndexes)
{
row=pmi.row;
column=pmi.column;
dummyTile=dummyBoard[row][column];
dummyBoard[row][column]=sourcePiece;
if(pieceIsKing==false)capturingPiecesMovesList=isPieceInDanger(dummyBoard,pieceColor,kingRowIndex,kingColumnIndex,false);
else capturingPiecesMovesList=isPieceInDanger(dummyBoard,pieceColor,row,column,false);
for(PieceMoves pieceMoves:capturingPiecesMovesList)
{
possibleMoves=pieceMoves.possibleMoves;
if(pieceIsKing==true)
{
if(possibleMoves[row][column]==1)
{
validPossibleMoves[row][column]=0;
possibleMovesSize--;
break;
}
}
else
{
if(possibleMoves[kingRowIndex][kingColumnIndex]==1)
{
validPossibleMoves[row][column]=0;
possibleMovesSize--;
break;
}
}//else ends 
}//for loop ends(possibleMovesCapture)
//put the piece on it's original position
//or reset the dummyBoard as before after simulating
dummyBoard[row][column]=dummyTile;
}
if(calledForStalemate==1 && possibleMovesSize<=0)
{
return null;
}
return validPossibleMoves;
}


public static ArrayList<PieceMoves> isPieceInDanger(byte [][]board,byte pieceColor,byte rowIndex,byte columnIndex,boolean includeAllValidPieces)
{
byte opponentPiece;
PieceMoves pieceMoves;
ArrayList<PieceMoves> piecesMoves;
piecesMoves=new ArrayList<>();
byte[][] possibleMoves;
KingCastling kingCastling=new KingCastling();
kingCastling.checkCastling=false;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
opponentPiece=board[e][f];
if(opponentPiece==0)
{
continue;
}

if((opponentPiece<0 && pieceColor==0) || (opponentPiece>0 && pieceColor==1))
{
//same piece , then skip
 continue;
}
possibleMoves=PossibleMoves.getPossibleMoves(board,e,f,kingCastling);
if(possibleMoves[rowIndex][columnIndex]==1)
{
pieceMoves=new PieceMoves();
pieceMoves.possibleMoves=possibleMoves;
pieceMoves.rowIndex=e;
pieceMoves.columnIndex=f;

piecesMoves.add(pieceMoves);
if(includeAllValidPieces==false)
{
return piecesMoves;
}
}
}
}
return piecesMoves;
}
public static boolean detectCheckmate(byte [][]board,byte color)
{
KingCastling kingCastling=new KingCastling();
kingCastling.checkCastling=false;
byte kingPiece=(byte)((color>0)?6:-6);
byte kingRowIndex=0;
byte kingColumnIndex=0;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if(board[e][f]==kingPiece)
{
kingRowIndex=e;
kingColumnIndex=f;
break;
}
}
}
ArrayList<PieceMoves> piecesMoves=isPieceInDanger(board,color,kingRowIndex,kingColumnIndex,true);
if(piecesMoves.size()==0) 
{
return false;
}

byte [][]kingPossibleMoves=PossibleMoves.getPossibleMoves(board,kingRowIndex,kingColumnIndex,kingCastling);
PossibleMovesIndex kingValidIndex;
ArrayList<PossibleMovesIndex> kingValidIndexes=new ArrayList<>();
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if(kingPossibleMoves[e][f]==1)
{
kingValidIndex=new PossibleMovesIndex();
//
kingValidIndex.row=e;
kingValidIndex.column=f;
kingValidIndex.safe=true;
kingValidIndexes.add(kingValidIndex);
}
}
}//king valid indexes loop ends here



if(kingValidIndexes.size()==0 && piecesMoves.size()>1)
{
//if king dont have any valid move and king is in threat by more than 1 opponent piece
//
return true;
}


PieceMoves attackingPieceMoves=piecesMoves.get(0);
byte[][] possibleMoves;
//creating a dummy tiles
//without the king which is in danger
byte [][]dummyBoard=new byte[8][8];
byte dummyTile;
byte tile;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
tile=board[e][f];
if(e==kingRowIndex && f==kingColumnIndex)
{
dummyBoard[e][f]=0;
}
else
{
dummyBoard[e][f]=tile;
}
}
}//creating dummy board(D.S) ends here
int kingValidIndexesCount=kingValidIndexes.size();
byte row,column;
ArrayList<PieceMoves> capturingPiecesMovesList;
ArrayList<PossibleMovesIndex> kingSafeIndexes=new ArrayList<>();
for(PossibleMovesIndex kvi:kingValidIndexes)
{
row=kvi.row;
column=kvi.column;
dummyTile=dummyBoard[row][column];
dummyBoard[row][column]=kingPiece;
capturingPiecesMovesList=isPieceInDanger(dummyBoard,color,row,column,true);
//
for(PieceMoves pieceMoves:capturingPiecesMovesList)
{
possibleMoves=pieceMoves.possibleMoves;
//
//
if(possibleMoves[row][column]==1)
{
//
kvi.safe=false;
break;
}
}
if(kvi.safe==true)
{
kingSafeIndexes.add(kvi);
}
dummyBoard[row][column]=dummyTile;
}
boolean safeTile=true;
if(kingSafeIndexes.size()==0)
{
safeTile=false;
for(PossibleMovesIndex kvi:kingValidIndexes)
{
//
}
}
else
{
//
//
for(PossibleMovesIndex kvi:kingSafeIndexes)
{
//
}
return false;
}
row=attackingPieceMoves.rowIndex;
column=attackingPieceMoves.columnIndex;
possibleMoves=attackingPieceMoves.possibleMoves;

byte opponentPiece=board[row][column];
byte attackingPieceRowIndex=row;
byte attackingPieceColumnIndex=column;
boolean captureOpponentPiece=false;
boolean blockOpponentPiece=false;
boolean knightPiece=false;
PossibleMovesIndex attackingPiecePossibleMovesIndex;
ArrayList<PossibleMovesIndex> attackingPiecePossibleMovesIndexes=new ArrayList<>();
if(opponentPiece==2 || opponentPiece==-2)
{
knightPiece=true;
}

else
{
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if(possibleMoves[e][f]==1)
{
attackingPiecePossibleMovesIndex=new PossibleMovesIndex();
attackingPiecePossibleMovesIndex.row=e;
attackingPiecePossibleMovesIndex.column=f;
attackingPiecePossibleMovesIndexes.add(attackingPiecePossibleMovesIndex);
}
}//inner loop
}//outer loop
}//attacking piece (possible moves indexes)
byte row1=0;
byte column1=0;
byte piece;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
piece=board[e][f];
if(piece==0) continue;
if( (piece<0 && color==1) || (piece>0 && color==0)) continue; //in case of opponent piece

if(piece==kingPiece) continue;//kingPiece represents either (blackKing or whiteKing)
possibleMoves=PossibleMoves.getPossibleMoves(board,e,f,kingCastling);
if(possibleMoves[attackingPieceRowIndex][attackingPieceColumnIndex]==1)
{
// the which is threating the king can be captured
captureOpponentPiece=true;
break;
}
//for blocking
if(knightPiece==false)
{
//creating dummyTiles
dummyBoard=generateDummyBoard(board,piece);
ArrayList<PossibleMovesIndex> friendlyPiecePossibleMovesIndexes=getPossibleMovesIndexesList(possibleMoves);
for(PossibleMovesIndex pmi:friendlyPiecePossibleMovesIndexes)
{
row1=pmi.row;
column1=pmi.column;
dummyBoard[row1][column1]=piece;

byte[][] opponentPiecePossibleMoves=PossibleMoves.getPossibleMoves(dummyBoard,attackingPieceRowIndex,attackingPieceColumnIndex,kingCastling);
if(opponentPiecePossibleMoves[kingRowIndex][kingColumnIndex]==0)
{
//the piece is blocked
//
blockOpponentPiece=true;
break; 
}
/*
ArrayList<PieceMoves> blockingPiece=isPieceInDanger(dummyTiles,kingRowIndex,kingColumnIndex);
if(blockingPiece.size()==0)
{
//the piece is blocked
blockOpponentPiece=true;
break; 
}
*/
dummyBoard[row1][column1]=0;
}
if(blockOpponentPiece==true) 
{
//
break;//done
}
}
}
}// part of blocking or capturing end's here	
if(captureOpponentPiece==false && blockOpponentPiece==false ) 
{
//
return true;
}
/*
if(blockOpponentPiece==false)
{
//
return true;
}
*/
//


return false;
}
private static byte[][] generateDummyBoard(byte[][] board,byte pieceNotToInclude)
{
byte [][]dummyBoard=new byte[8][8];
byte dummyTile;
byte tile;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
tile=board[e][f];
dummyBoard[e][f]=(tile!=pieceNotToInclude)?tile:0;
}
}//creating dummy tiles(D.S) ends here
return dummyBoard;
}
private static ArrayList<PossibleMovesIndex> getPossibleMovesIndexesList(byte [][]possibleMoves)
{
ArrayList<PossibleMovesIndex> possibleMovesIndexes=new ArrayList<>();
PossibleMovesIndex possibleMovesIndex;
for(byte e=0;e<8;e++)
{
for(byte f=0;f<8;f++)
{
if(possibleMoves[e][f]==1)
{
possibleMovesIndex=new PossibleMovesIndex();
possibleMovesIndex.row=e;
possibleMovesIndex.column=f;
possibleMovesIndexes.add(possibleMovesIndex);
}
}//inner loop
}//outer loop
return possibleMovesIndexes;
}
}
