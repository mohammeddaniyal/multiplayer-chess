package io.github.mohammeddaniyal.chess.server.models;
import io.github.mohammeddaniyal.chess.common.Move;
import java.util.*;
public class Game implements java.io.Serializable
{
public String id;
public String player1;
public String player2;
public byte[][] board;
public byte[][] possibleMoves;
public byte activePlayer;
public byte isStalemate=0;
public List<Move> moves;
public KingCastling whiteKingCastling;
public KingCastling blackKingCastling;
}
