package io.github.mohammeddaniyal.chess.server;

import io.github.mohammeddaniyal.chess.server.handler.MoveHandler;
import io.github.mohammeddaniyal.chess.server.models.Game;
import io.github.mohammeddaniyal.chess.server.models.KingCastling;
import io.github.mohammeddaniyal.chess.server.utils.ChessLogger;
import io.github.mohammeddaniyal.chess.server.logic.*;
import io.github.mohammeddaniyal.chess.common.*;
import io.github.mohammeddaniyal.nframework.server.*;
import io.github.mohammeddaniyal.nframework.server.annotations.*;
import io.github.mohammeddaniyal.chess.server.dl.*;
import java.util.*;

@Path("/ChessServer")
public class ChessServer {
    static private final long TIMEOUT_DURATION = 30 * 1000; // 30 seconds in millisecond
    static private Map<String, Member> members;
    static private Set<String> loggedInMembers;
    static private Set<String> playingMembers;
    static private Map<String, List<Message>> inboxes;
    static private Map<String, Message> invitationsTimeout;
    static private Map<String, List<String>> userExpiredInvitations;
    static private Map<String, GameInit> gameInits;
    static private Map<String, Game> games;
    static private final byte WHITE = 1;
    static private final byte BLACK = 0;
    static {
        populateDataStructures();
    }

    public ChessServer() {
    }

    static private void populateDataStructures() {
        try {
            MemberDAO memberDAO = new MemberDAO();
            List<MemberDTO> dlMembers = memberDAO.getAll();
            Member member;
            members = new HashMap<>();
            for (MemberDTO memberDTO : dlMembers) {
                member = new Member();
                member.username = memberDTO.username;
                member.password = memberDTO.password;
                members.put(member.username, member);
            }
            loggedInMembers = new HashSet<>();
            playingMembers = new HashSet<>();
            inboxes = new HashMap<>();
            invitationsTimeout = new HashMap<>();
            userExpiredInvitations = new HashMap<>();
            gameInits = new HashMap<>();
            games = new HashMap<>();
            ChessLogger.log.info("Successfully loaded " + dlMembers.size() + " members from database.");
        } catch (Exception e) {
            ChessLogger.log.severe("FAILED to load members from database: " + e.getMessage());
        }
    }

    @Path("/authenticateMember")
    public boolean isMemberAuthentic(String username, String password) {
        Member member = members.get(username);
        if (member == null)
            return false;
        boolean b = password.equals(member.password);
        if (b) {
            loggedInMembers.add(username);
            ChessLogger.log.info("User Authenticated: " + username);
        } else {
            ChessLogger.log.warning("Failed login attempt for user: " + username);
        }
        return b;
    }

    @Path("/logout")
    public void logout(String username) {
        loggedInMembers.remove(username);
        ChessLogger.log.info("User Logged Out: " + username);
    }

    @Path("/getMembers")
    public List<MemberInfo> getMembers(String username) {
        List<MemberInfo> membersInfo = new LinkedList<>();
        MemberInfo memberInfo;
        String u;
        // determining the status of each member (by using wisely other two
        // sets[playingMember and loggedInMembers]) and adding into the list

        for (var entry : members.entrySet()) {
            // getting username from map
            u = entry.getKey();

            // no need to add that user who asked or called this method getMembers
            // exclude this user
            if (u.equals(username))
                continue;

            memberInfo = new MemberInfo();
            memberInfo.member = u;
            // player is online
            if (loggedInMembers.contains(u))
                memberInfo.status = PLAYER_STATUS_TYPE.ONLINE;
            // player is online but in game
            else if (playingMembers.contains(u))
                memberInfo.status = PLAYER_STATUS_TYPE.IN_GAME;
            // player is offline
            else
                memberInfo.status = PLAYER_STATUS_TYPE.OFFLINE;
            membersInfo.add(memberInfo);
        }

        return membersInfo;
    }

    @Path("/inviteUser")
    public void inviteUser(String fromUsername, String toUsername) {
        Message message = new Message();
        message.fromUsername = fromUsername;
        message.toUsername = toUsername;
        message.type = MESSAGE_TYPE.CHALLENGE;
        message.inviteTimeStamp = System.currentTimeMillis();
        this.invitationsTimeout.put(fromUsername, message);
        List<Message> messages = inboxes.get(toUsername);
        if (messages == null) {
            messages = new LinkedList<Message>();
            inboxes.put(toUsername, messages);
        }
        messages.add(message);
    }

    @Path("/invitationReply")
    public void invitationReply(Message m) {
        Message message = new Message();
        message.fromUsername = m.fromUsername;
        message.toUsername = m.toUsername;
        message.type = m.type;
        List<Message> messages = inboxes.get(message.toUsername);
        if (messages == null) {
            messages = new LinkedList<>();
            this.inboxes.put(message.toUsername, messages);
        }
        messages.add(message);
        String fromUsername = message.toUsername;
        String toUsername = message.fromUsername;
        if (message.type == MESSAGE_TYPE.CHALLENGE_ACCEPTED) {
            // and remove from logged in members
            loggedInMembers.remove(fromUsername);
            loggedInMembers.remove(toUsername);
            // add both this player into playingMembers set
            playingMembers.add(fromUsername);
            playingMembers.add(toUsername);

            String uuid = UUID.randomUUID().toString();
            Random random = new Random();
            // decide player color

            byte playerColor1 = (byte) random.nextInt(2); // Generates 0 or 1 (player [fromUsername])
            byte playerColor2 = (playerColor1 == WHITE ? BLACK : WHITE); // (player [toUsername])

            // create Game object(for this session of game of this two players)
            Game game = new Game();
            game.id = uuid;
            game.player1 = fromUsername;
            game.player2 = toUsername;
            game.board = BoardInitializer.initializeBoard();
            ChessLogger.log
                    .info("Match Started: " + fromUsername + " (White) vs " + toUsername + " (Black). GameID: " + uuid);
            // decide which will be the first to play
            // since WHITE represent 1 and BLACK represent 0
            game.activePlayer = (byte) random.nextInt(2); // Generates 0 or 1
            game.moves = new LinkedList<Move>();

            game.whiteKingCastling = new KingCastling();
            game.blackKingCastling = new KingCastling();

            this.games.put(uuid, game);

            GameInit gameInit = new GameInit();
            gameInit.gameId = uuid;
            gameInit.playerColor = playerColor1;
            gameInit.board = BoardInitializer.initializeBoard();
            // putting player 1
            this.gameInits.put(fromUsername, gameInit);

            // player 2
            gameInit = new GameInit();
            gameInit.gameId = uuid;
            gameInit.playerColor = playerColor2;
            gameInit.board = BoardInitializer.initializeBoard();
            // putting player 2
            this.gameInits.put(toUsername, gameInit);
        }
        message = this.invitationsTimeout.get(fromUsername);
        if (message == null)
            return;
        if (message.toUsername.equals(toUsername)) {
            this.invitationsTimeout.remove(fromUsername);
        }
    }

    @Path("/getMessages")
    public List<Message> getMessages(String username) {
        List<Message> messages = inboxes.get(username);
        if (messages != null && messages.size() > 0) {
            inboxes.put(username, new LinkedList<Message>());
        }
        return messages;
    }

    @Path("/expiredInvitations")
    public List<String> getExpiredInvitations(String username) {
        List<String> invitationsExpiredOf = this.userExpiredInvitations.get(username);
        if (invitationsExpiredOf != null && invitationsExpiredOf.size() > 0) {
            this.userExpiredInvitations.put(username, new LinkedList<>());
        }
        return invitationsExpiredOf;
    }

    @Path("/getInvitationStatus")
    public Message getInvitationStatus(String fromUsername, String toUsername) {
        Message message = this.invitationsTimeout.get(fromUsername);
        if (message != null) {
            long sentTime = message.inviteTimeStamp;
            long currentTime = System.currentTimeMillis();
            if (currentTime - sentTime >= TIMEOUT_DURATION) {
                // add the expired invitation to userExpiredInvitation map
                List<String> invitationsFrom = userExpiredInvitations.get(toUsername);
                if (invitationsFrom == null) {
                    invitationsFrom = new LinkedList<>();
                    this.userExpiredInvitations.put(toUsername, invitationsFrom);
                }
                invitationsFrom.add(fromUsername);
                // player ignored the invitation
                // remove the message from invitationsTimeout
                this.invitationsTimeout.remove(fromUsername);
                message = new Message();
                message.fromUsername = toUsername;
                message.toUsername = fromUsername;
                message.type = MESSAGE_TYPE.CHALLENGE_IGNORED;

                return message;
            }
        } // if the user didn't respond to the invitation then this part of ignored
          // invitation
        return null;
    }

    @Path("/getGameInit")
    public GameInit getPlayerIdentity(String username) {
        // Instantly check the map. No loops! No sleeping!
        GameInit gameInit = this.gameInits.get(username);

        // If a game was found, remove it so we don't send it twice
        if (gameInit != null) {
            this.gameInits.remove(username);
        }

        // This will return the game if found, or 'null' immediately if not.
        return gameInit;
    }

    @Path("/canIPlay")
    public boolean canIPlay(String gameId, byte playerColor) {
        Game game = games.get(gameId);
        if (game == null) {
            return false;
        }

        return game.activePlayer == playerColor;
    }

    @Path("/getPossibleMoves")
    public byte[][] getPossibleMoves(String gameId, byte fromX, byte fromY) {
        Game game = games.get(gameId);
        if (game == null)
            return new byte[8][8];
        return MoveHandler.getPossibleMoves(game, fromX, fromY);
    }

    @Path("/submitMove")
    public MoveResponse submitMove(Move m, String gameId) {
        Game game = games.get(gameId);
        if (game == null)
            return null;
        Move move = new Move();
        move.player = m.player;
        move.piece = m.piece;
        move.fromX = m.fromX;
        move.fromY = m.fromY;
        move.toX = m.toX;
        move.toY = m.toY;
        move.isLastMove = m.isLastMove;
        move.castlingType = m.castlingType;
        move.pawnPromotionTo = m.pawnPromotionTo;
        move.isInCheck = m.isInCheck;
        move.ambiguityType = m.ambiguityType;
        MoveResponse moveResponse = MoveHandler.validateMove(game, move);
        if (moveResponse.isValid == 0)
            return moveResponse;
        byte isLastMove = MoveHandler.detectCheckmate(game);
        move.isLastMove = isLastMove;
        moveResponse.isLastMove = isLastMove;
        // if it's not checkmate condition then check for "CHECK"
        if (isLastMove == 0) {
            byte isInCheck = MoveHandler.detectCheck(game, move);
            move.isInCheck = isInCheck;
            moveResponse.isInCheck = isInCheck;
        }

        // update the move in list
        game.moves.add(move);
        // switch the player
        byte playerColor = move.player;
        game.activePlayer = (byte) ((playerColor == 1) ? 0 : 1);
        ChessLogger.log.info(
                "Move played in Game " + gameId + " by player " + move.player + " to " + move.toX + "," + move.toY);
        return moveResponse;
    }

    @Path("/getOpponentMove")
    public Move getOpponentMove(String gameId, byte playerColor) {
        Game game = games.get(gameId);
        if (game == null)
            return null;
        if (game.activePlayer != playerColor) {
            return null;
        }
        int size = game.moves.size();
        int lastMoveIndex = (size > 0) ? size - 1 : 0;
        return game.moves.get(lastMoveIndex);
    }

    // this method will check for a specific player that it has any legal move
    @Path("/isStalemate")
    public byte isStalemate(String gameId, byte player) {
        Game game = games.get(gameId);
        if (game == null)
            return -1;
        byte stalemate = StalemateDetector.detectStalemate(game, (byte) player);
        game.isStalemate = stalemate;
        return stalemate;
    }

    // this method tells the opponent stalemate occurs
    @Path("/stalemateOccur")
    public byte checkStalemateOccur(String gameId) {
        Game game = games.get(gameId);
        if (game == null)
            return -1;
        return game.isStalemate;
    }

    @Path("/leftGame")
    public void leftGame(String username) {
        this.playingMembers.remove(username);
        this.loggedInMembers.add(username);
    }

    @Path("/isOpponentLeftTheGame")
    public byte isOpponentLeftTheGame(String gameId, String username) {
        Game game = games.get(gameId);
        if (game == null)
            return -1;
        String opponentPlayer = (game.player1.equals(username)) ? game.player2 : game.player1;
        // search the opponent is he still in the playingMembers set or not
        // if yes then he doesn't left the game and otherwise he left
        byte leftTheGame = (byte) ((playingMembers.contains(opponentPlayer)) ? 0 : 1);
        return leftTheGame;
    }
}
