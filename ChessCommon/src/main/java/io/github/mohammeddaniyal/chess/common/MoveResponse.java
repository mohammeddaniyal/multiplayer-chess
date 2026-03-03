package io.github.mohammeddaniyal.chess.common;
public class MoveResponse implements java.io.Serializable
{
public byte isValid;
public byte isLastMove=0;
public byte castlingType=0;
//0 represents no castling
//1 white kingside
//2 white queenside castling
//3 black king side
//4 black queen side
public byte pawnPromotionTo=0;
//for white 
//1 represents no promotion(pawn)
//6 promote to Queen
//4 promote to Rook
//3 promote to Bishop
//2 promote to Knight
//for black all negative values
//for generating pgn notation
public byte isInCheck=0;
public byte ambiguityType=0;
//0 for none ambiguity
//1 for file ambiguity
//2 for rank ambiguity
}
