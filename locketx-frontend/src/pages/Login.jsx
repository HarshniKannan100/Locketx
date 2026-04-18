import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import "../App.css";
function Login() {
    const [userName,setUserName] = useState("");
    const [password,setPassword] = useState("");
    const navigate = useNavigate();

    const login = async () => {
        const response = await fetch("http://localhost:8080/api/auth/login",{
            method:"POST",
            headers:{"Content-Type":"application/json"},
            body: JSON.stringify({username:userName,password})
        });
        const data=await response.json(); 
        if(data.token){
            alert("Login successful!");
            localStorage.setItem("token", data.token);
            localStorage.setItem("id", data.id);
            localStorage.setItem("name", data.name);
            console.log(data.id+" "+data.token+" "+data.name);
            navigate("/home");
        }else{
            alert("Login failed: " + data);
        }   
    };

    return (
        <div className='login-container'>
            <h2>Login</h2>
            <input type="text" placeholder="Username" value={userName} onChange={(e) => setUserName(e.target.value)} />
            <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
            <button onClick={login}>Login</button>
            <p>Don't have an account? <button onClick={() => navigate("/register")}>Register</button></p>
        </div>
    );
}
export default Login;