package io.github.mohammeddaniyal.chess.client.history;
import io.github.mohammeddaniyal.chess.common.Move;
public class PGNConvertor
{
public static String convertMoveToPGN(Move move,byte isCapture)
{
StringBuilder pgn=new StringBuilder();

if(move.castlingType==1 || move.castlingType==3) return "O-O";
else if(move.castlingType==2 || move.castlingType==4) return "O-O-O";

//determine piece notation
char pieceChar=getPieceChar(move.piece);

if(pieceChar!=' ') pgn.append(pieceChar);

//handling ambiguity case
//file ambiguity
if(move.ambiguityType==1)
{
pgn.append(8-move.fromX);
}else if(move.ambiguityType==2){
//rank ambiguity
char fromRank=(char)('a'+move.fromY);
pgn.append(fromRank);
}

//capture notation
if(isCapture==1)
{
pgn.append('x');
}

//destination part notation
char toFile=(char)('a'+move.toY);
pgn.append(toFile);
pgn.append(8-move.toX);

// promotion notation
if(move.pawnPromotionTo!=0 && move.pawnPromotionTo!=1 && move.pawnPromotionTo!=-1)
{
pgn.append('=').append(getPieceChar(move.pawnPromotionTo));
}

// for checkmate
if(move.isLastMove==1)
{
pgn.append('#');
}else if(move.isInCheck==1)
{
pgn.append('+');
}

return pgn.toString();
}

private static char getPieceChar(byte piece)
{
piece=(byte)((piece<0)?piece*-1:piece);
return switch(piece){
case 1 -> ' ';//pawn no letter
case 2 -> 'N';
case 3 -> 'B';
case 4 -> 'R';
case 5 -> 'Q';
case 6 -> 'K';
default -> ' ';
};
}
}
