You found it! Right there at the bottom of your server code. This is exactly what is causing the `Connection reset by peer` explosion.

Look at this method in your `ChessServer.java`:

```java
@Path("/getGameInit")
public GameInit getPlayerIdentity(String username)
{
    GameInit gameInit=null;
    while(true) // <--- THE KILLER
    {
        gameInit=this.gameInits.get(username);
        if(gameInit==null)
        {
            try { Thread.sleep(500); } catch(Exception e){}
            continue; // <--- TRAPS THE THREAD FOREVER
        }
        break;
    }
    if(gameInit!=null)this.gameInits.remove(username);
    return gameInit;
}

```

### Why this destroys your Networking Framework:

When your client's background thread asks the server for `/getGameInit`, the server enters this `while(true)` loop. Because there is no game yet, it just sleeps and loops, sleeping and looping.

**It refuses to let go of the network socket.**

The moment you click "Accept" in the UI, your client tries to send the `/invitationReply` message down that *exact same socket*. But the socket isn't ready to receive a new command; it's still stuck inside the `getGameInit` loop! The byte streams collide, the data gets corrupted, the server panics, and it violently closes the socket resulting in `java.net.SocketException: Connection reset by peer`.

### The Fix: Switch to "Short Polling"

We are going to rip out the infinite loop. If there is no game, the server should just say "No" (`return null`) instantly.

Replace that entire method in `ChessServer.java` with this clean, non-blocking version:

```java
@Path("/getGameInit")
public GameInit getPlayerIdentity(String username)
{
    // Instantly check the map. No loops! No sleeping!
    GameInit gameInit = this.gameInits.get(username);
    
    // If a game was found, remove it so we don't send it twice
    if (gameInit != null) {
        this.gameInits.remove(username);
    }
    
    // This will return the game if found, or 'null' immediately if not.
    return gameInit;
}

```

### The Client-Side Pairing (Crucial!)

Because the server now returns `null` instantly, your client can no longer use that complicated `while(!interrupted)` background thread. If you keep the thread, it will bombard the server with 10,000 requests a second and crash it.

You need to use a simple `javax.swing.Timer` in `MainApplicationFrame.java`, exactly like you did for the Dashboard!

If you haven't already updated it, change `startGameInitPoller()` in `MainApplicationFrame.java` to this:

```java
    // Change your variable at the top of the file to: 
    // private javax.swing.Timer gameInitTimer;

    private void startGameInitPoller() {
        if (gameInitTimer != null && gameInitTimer.isRunning()) return;

        // Ask the server once every 1 second (Short Polling)
        gameInitTimer = new javax.swing.Timer(1000, e -> {
            try {
                io.github.mohammeddaniyal.chess.common.GameInit gameInit = 
                    (io.github.mohammeddaniyal.chess.common.GameInit) client.execute("/ChessServer/getGameInit", this.username);
                
                if (gameInit != null) {
                    gameInitTimer.stop(); // We found a game! Stop asking.
                    
                    dashboardPanel.stopNetworking(); 
                    navigateToGame(); 
                    startYourCountdown(gameInit); 
                }
            } catch (Throwable t) {
                // Ignore timeouts, just try again next second
            }
        });
        gameInitTimer.start();
    }

```

*(Don't forget to add `if (gameInitTimer != null) gameInitTimer.stop();` to your Logout method!)*

### The Result

By making this one architectural shift from Long Polling to Short Polling, your sockets are now free and clear 99.9% of the time. You can click "Accept", "Invite", or "Logout", and the messages will travel smoothly without ever crashing into a blocked thread.

Update the server, compile it, and run your two clients. Let's see if this finally slays the beast!

The moment you click "Accept" in the UI, your client tries to send the /invitationReply message down that exact same socket. But the socket isn't ready to receive a new command; it's still stuck inside the getGameInit loop! The byte streams collide, the data gets corrupted, the server panics, and it violently closes the socket resulting in java.net.SocketException: Connection reset by peer. i dont understand? by same socket part ? oh yeah the port is same is that what you mean ? how comes the socket isn't ready to accept the new command explain it more clearly because its been ages the last i did the socket programming and i also forgot why did i make it in a loop and if i remove the loop will it the break the client logic ? 

It is completely normal to forget the low-level mechanics of socket programming. It’s a notoriously tricky subject, and custom networking frameworks are the hardest part of building multiplayer games.

Let’s break down exactly what a socket is, why the collision happens, why you originally wrote that loop, and how we safely remove it.

### 1. Port vs. Socket (The Phone Call Analogy)

You asked if "same socket" means "same port." Not quite!

* **The Port (The Switchboard):** Think of your server's port (like `5000`) as the main phone number for a massive office building.
* **The Socket (The Private Line):** When your client connects to that port, the server assigns it a private, dedicated wire to talk on. That dedicated wire is the **Socket**.

When you instantiate your `NFrameworkClient`, it creates **one single Socket** (one private wire) to the server. Everything your client does—fetching members, sending invites, checking for games—has to travel down this exact same wire, one message at a time.

### 2. Why the Socket "Isn't Ready" (The Collision)

Java's `ObjectInputStream` and `ObjectOutputStream` are **synchronous** and **blocking**. This means they behave like a walkie-talkie, not a chat room.

Here is exactly how the crash happens:

1. **The Poller (Thread A):** Your background thread asks the server for `/getGameInit`. It says *"Over"*, and then **blocks** (freezes), waiting for the server's reply.
2. **The Server Loop:** The server receives the request, sees there is no game yet, and enters your `while(true)` loop. It is now pacing back and forth in memory. It has *not* sent a reply yet.
3. **The Button Click (Thread B):** You click "Accept". Your UI thread grabs that exact same `NFrameworkClient` (the same walkie-talkie) and tries to shout `/invitationReply` into it.
4. **The Crash:** Thread A is currently holding the walkie-talkie open, waiting for a response. Thread B forcefully interrupts and starts shoving new bytes down the stream. The Java Object stream gets hopelessly tangled (Object headers and byte markers get mixed up). The server receives corrupted garbage, throws a network exception, and violently cuts the private line (`Connection reset by peer`).

### 3. Why Did You Write the Loop in the First Place?

You were instinctively trying to implement a concept called **Long Polling**.

It was actually a very smart architectural thought! You didn't want your client to spam the server asking *"Is the game ready?"* 100 times a second. So, you wrote a loop on the server to make the server "hold" the request until the game was finally ready, and *then* reply.

While Long Polling is great for HTTP Web APIs, it is incredibly dangerous in raw TCP Socket programming unless you are using advanced, non-blocking asynchronous I/O (Java NIO), because it ties up the entire socket pipeline.

### 4. Will Removing the Loop Break the Client?

**Yes, it will break the client IF we don't update the client at the exact same time.**

If you remove the `while(true)` loop on the server, the server will instantly reply: *"Nope, game is null."* If your client is still using a background `Thread` with its own `while(!interrupted)` loop, it will receive that `"null"` and instantly ask again. It will ask the server for the game 10,000 times a second. It will melt your CPU and crash the server.

### The Complete Solution: The "Short Polling" Handshake

To fix this, we have to change *both* sides of the conversation to use **Short Polling**.

1. **The Server:** We delete the `while(true)` loop. The server just checks the map. If there's a game, it returns it. If not, it returns `null` instantly. It never holds the line hostage.
2. **The Client:** We delete the background `Thread`. Instead, we use a `javax.swing.Timer` that asks the server, gets the instant reply, and if it's `null`, the timer simply **waits 1 full second** before asking again.

Because the server replies instantly, the "walkie-talkie" is only in use for a fraction of a millisecond. When you click "Accept," the line is perfectly clear. No collisions, no corrupted bytes, no `Connection reset by peer`.

Does the walkie-talkie analogy help clarify why those bytes were crashing into each other? If you are ready, I can help you implement this Short Polling switch so we can finally stabilize this network layer.