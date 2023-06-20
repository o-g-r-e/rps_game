let stompClient;
let userId;

function onReceive(data) {
    console.log("received data: ", data);
    let responseObject = JSON.parse(data.body)
    appendMessage(responseObject);

    if(responseObject.command == "signup" || responseObject.command == "logout") {
        disconnect();
    } else if(responseObject.command == "signin") {
        userId = responseObject.userId;
    } else if(responseObject.command == "" && responseObject.status == "timeExpired") {
        sendMessage("/nextStage", {userId});
    }
}

function appendMessage(responseObject) {
    let currentConsoleValue = $("#textarea").val();
    let mesSep = "\n---------------------------------------------------------\n";
    $("#textarea").val(currentConsoleValue+mesSep+"Command: "+responseObject.command+"\nResponse Status: "+responseObject.status+"\nResponse message:\n"+responseObject.message+mesSep);
    $("#textarea").scrollTop($("#textarea")[0].scrollHeight);
}

function appendIncomingStatus(text) {
    let currentConsoleValue = $("#textarea").val();
    let mesSep = "\n---------------------------------------------------------\n";
    $("#textarea").val(currentConsoleValue+mesSep+text+mesSep);
    $("#textarea").scrollTop($("#textarea")[0].scrollHeight);
}

function connect(cb) {
    let socket = new SockJS('/our-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, cb);
}

function disconnect() {
    userId = undefined;
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    $("#textarea").val($("#textarea").val()+'\nDISCONNECTED\n');
}

function sendMessage(url, obj) {

    if(stompClient == null) {
        $("#textarea").val($("#textarea").val() + "\nConnect to server first\n");
        return;
    }

    stompClient.send("/app"+url, {}, JSON.stringify(obj));
}

function connectWith(inputValue) {
    connect(function (frame) {
        $("#textarea").val($("#textarea").val()+'\nCONNECTED\n');
        stompClient.subscribe('/users/queue/messages', onReceive);

        let matchResult = inputValue.match(/(signin|signup)=(.+)=(.+)/);
        let connectCommand = matchResult[1];
        let userName = matchResult[2];
        let password = matchResult[3];
        sendMessage("/"+connectCommand, {command: connectCommand, data:userName+" "+password});
    });
}

$(function () {

    $("#textarea").val(`
---------------------------------------------------------
Welcome to RPS Game!
Now you access next commands:
singup, singin
---------------------------------------------------------
`);

    $("#input").keydown(function(e) {
        if (e.keyCode == 13 && !e.shiftKey) {
            e.preventDefault();
            let inputValue = $("#input").val().trim();
            if(inputValue.startsWith("signin=") || inputValue.startsWith("signup=")) {

                connectWith(inputValue);

            } else if(inputValue.startsWith("logout")) {

                sendMessage("/logout", {command: "logout", userId});

            } else if(inputValue.startsWith("start")) {

                sendMessage("/startGame", {command: "start", userId});

            } else if(inputValue.startsWith("rock")) {

                sendMessage("/userGuess/rock", {command: "rock", userId});

            } else if(inputValue.startsWith("paper")) {

                sendMessage("/userGuess/paper", {command: "paper", userId});

            } else if(inputValue.startsWith("scissors")) {

                sendMessage("/userGuess/scissors", {command: "scissors", userId});

            }
            $("#input").val("");
            return false;
        }
    });
});

