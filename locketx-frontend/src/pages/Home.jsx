import { useState,useRef, useEffect} from "react";
import "../App.css";
function Home() {
    const [userId, setUserId] = useState(null);
    const [userName,setUserName] = useState("");
    useEffect(() => {
        const id = localStorage.getItem("id");
        const name=localStorage.getItem("name");
        setUserId(id ? Number(id) : null);
        setUserName(name?String(name):"");
    }, []);

    
    const [messages,setMessages] = useState([]);;
    const [availableUsers,setAvailableUsers] = useState([]);
    const [selectedUsers,setSelectedUsers] = useState([]);
    const [file,setFile] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const videoRef = useRef(null);
    const socketRef = useRef(null);

    const connect = () => {
        const token=localStorage.getItem("token");
        if (!token) {
            console.log("No token found");
            return;
        }
        const socket=new WebSocket(`ws://localhost:8080/chat?token=${token}`);
        socket.onmessage=(event)=>{
            console.log("RAW MESSAGE:", event.data);
            const msg=JSON.parse(event.data);
            console.log("Users received:", msg.users); 
            if(msg.type==="USERS"){
                setAvailableUsers(msg.users);
            }else{
                setMessages((prev)=>[...prev,msg]);
            }
        };
        console.log("WebSocket connected");
        setIsConnected(true);
        socketRef.current=socket;
    };

    useEffect(() => {
        if (userId !== null) {
            connect();
        }
    }, [userId],[userName]);

    const toggleUser = (id) => {
        setSelectedUsers((prev) =>
        prev.includes(id)
            ? prev.filter((u) => u !== id)
            : [...prev, id]
        );
    };

    const uploadAndSend = () => {
      if (!isConnected) {
        alert("Connect first!");
        return;
      }

      if (!file) {
        alert("Select a file");
        return;
      }

      if (selectedUsers.length === 0) {
        alert("Select at least one user");
        return;
      }

      const formData = new FormData();
        formData.append("file", file);

        fetch("http://localhost:8080/api/images/upload", {
        method: "POST",
        body: formData
        })
        .then(res => res.text())
        .then(url => {
            const message = {
            type: "IMAGE",
            senderId: userId,
            receiverIds: selectedUsers,
            content: url
            };

            socketRef.current.send(JSON.stringify(message));
        });
    };

    const startCamera = async () => {
        try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        videoRef.current.srcObject = stream;
        } catch (err) {
        console.error(err);
        alert("Camera access denied");
        }
    };

    const capturePhoto = () => {
        if (!socketRef.current || socketRef.current.readyState !== WebSocket.OPEN) {
        alert("Connect first!");
        return;
        }

        if (selectedUsers.length === 0) {
        alert("Select at least one user");
        return;
        }

        const video = videoRef.current;

        const canvas = document.createElement("canvas");
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;

        const ctx = canvas.getContext("2d");
        ctx.drawImage(video, 0, 0);

        canvas.toBlob((blob) => {
        const formData = new FormData();
        formData.append("file", blob, "photo.jpg");
        formData.append("userId", userId);
        fetch("http://localhost:8080/api/images/upload", {
            method: "POST",
            body: formData
        })
            .then(res => res.text())
            .then(url => {
            url = url.replace(/"/g, "");
            const message = {
                type: "IMAGE",
                senderId: parseInt(userId),
                receiverIds: selectedUsers,
                content: url
            };

            socketRef.current.send(JSON.stringify(message));
            });
        }, "image/jpeg");
    };
    return (
        <div className="home-container">
            <h2>Welcome, {userName}</h2>
            <h3>Active Users</h3>

          <div className="user-list">
            {availableUsers
              .filter((id) => id !== parseInt(userId))
              .map((id) => (
                <div
                  key={id}
                  onClick={() => toggleUser(id)}
                  className={`user-card ${
                    selectedUsers.includes(id) ? "active" : ""
                  }`}
                >
                  User {id}
                </div>
              ))}
          </div>

          {/* CAMERA */}
          <video ref={videoRef} className="video" autoPlay />
          <div className="camera-controls">
            <button className="button" onClick={startCamera}>
              Start Camera
            </button>
            <button className="capture-btn" onClick={capturePhoto}>
              👻
            </button>
          </div>

          <hr />

          {/* FEED */}
          <h3>Feed</h3>

          <div className="feed">
            {messages.map((msg, index) => (
              <div key={index} className="feed-card">
                {console.log("Message content:", msg.content)}
                <b>User {msg.senderId}</b>

                {msg.type === "IMAGE" && (
                  <img src={msg.content} className="feed-img" />
                )}
              </div>
            ))}
          </div>
        </div>

    );
}
export default Home;