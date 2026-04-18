import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import "../App.css";
function Register() {
    const [userName,setUserName] = useState("");
    const [password,setPassword] = useState("");
    const navigate = useNavigate();
    const register = async () => {
        const response = await fetch("http://localhost:8080/api/auth/register",{
            method:"POST",
            headers:{"Content-Type":"application/json"},
            body: JSON.stringify({username:userName,password})
        });
        const data=await response.text();
        if(data.includes("success")){
            alert("Registration successful! Please login.");
            navigate("/");
        }else{
            alert("Registration failed: " + data);
        }
    };
    return (
        <div className='register-container'>
            <h2>Register</h2>
            <input type="text" placeholder="Username" value={userName} onChange={(e) => setUserName(e.target.value)} />
            <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
            <button onClick={register}>Register</button>
            <p>Already have an account? <button onClick={() => navigate("/")}>Login</button></p>
        </div>
    );
}
export default Register;