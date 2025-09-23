import { useNavigate } from "react-router-dom";
import { assets } from "../assets/assets.js";
import { useContext, useState, useRef, useEffect } from "react";
import { AppContext } from "../context/AppContext.jsx";
import axios from "axios";
import {toast} from "react-toastify";

const Menubar = () => {
    const navigate = useNavigate();

    // useContext
    const {userData, backendURL, setUserData, setIsLoggedIn} = useContext(AppContext);

    // Creation of states
    const [dropdownOpen, setDropdownOpen] = useState(false);

    // useRef
    const dropdownRef = useRef(null);

    // useEffect for the dropdown to disappear when needed
    useEffect(() => {
        const handleClickOutside = (event) => {
            if(dropdownRef.current && !dropdownRef.current.contains(event.target)){
                setDropdownOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);

        return () => document.removeEventListener("mousedown", handleClickOutside); 
    }, []);

    const toLogout = async () => {
        try{
            axios.defaults.withCredentials = true;
            const response = await axios.post(backendURL + "/logout");

            if(response.status === 200){
                setIsLoggedIn(false);
                setUserData(false);
                navigate("/");
                toast.success("You have logged out successfully!")
            }
        }
        catch(error){
            toast.error(error.message);
        }
    }

    const sendVerificationOtp = async () => {
        try{
            axios.defaults.withCredentials = true;
            const response = await axios.post(backendURL + "/send-otp");
            if (response.status === 200){
                navigate("/email-verification");
                toast.success("OTP has been sent successfully!");
            }
            else{
                toast.error("Unable to send OTP!");
            }
        }
        catch(error){
            toast.error(error.message);
        }
    }

    return(
       <nav className="navbar bg-white px-5 py-4 d-flex justify-content-between align-items-center">
            {/* Logo and App Name */}
            <div className="d-flex align-items-center gap-2">
                <img src={assets.logo_home} alt="logo" width={32} height={32} />
                <span className="fw-bold fs-4 text-dark">Kuya Guard</span>
            </div>

            {/* User data */}
            {userData ? (
                <div className="position-relative" ref={dropdownRef}>
                    <div 
                        className="bg-dark text-white rounded-circle d-flex justify-content-center align-items-center"
                        style={{
                            width: "40px",
                            height: "40px",
                            cursor: "pointer",
                            userSelect: "none",
                        }}
                        onClick={()  => setDropdownOpen((previous) => !previous)}
                    >
                        {userData.name[0].toUpperCase()}
                    </div>

                    {dropdownOpen && (
                        <div 
                            className="position-absolute shadow bg-white rounded p-2"
                            style={{
                                top: "50px",
                                right: 0,
                                zIndex: 100,
                            }}
                        >
                            {/* Verify e-mail dropdown option - Only displays when the account is not yet verified */}
                            {!userData.isAccountVerified && (
                                <div 
                                    className="dropdown-item py-1 px-2"
                                    style={{cursor: "pointer"}}
                                    onClick={sendVerificationOtp}
                                >
                                    Verify e-mail
                                </div>
                            )}

                            <div 
                                className="dropdown-item py-1 px-2 text-danger"
                                style={{ cursor: "pointer" }}
                                onClick={toLogout}
                            >
                                Logout
                            </div>
                        </div>
                    )}
                </div>
                ) 
                : 
                (
                // Log-in button
                <div className="btn btn-outline-dark rounded-pill px-3" onClick={() => navigate("/login")}>
                    Login <i className="bi bi-arrow-right ms-2"></i>
                </div>
            )}
       </nav>
    );
}

export default Menubar;