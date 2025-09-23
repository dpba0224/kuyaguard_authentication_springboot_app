import { Link, useNavigate } from "react-router-dom"
import { useContext, useState, useRef, useEffect } from "react";
import { assets } from "../assets/assets";
import { AppContext } from "../context/AppContext";
import {toast} from "react-toastify";
import axios from "axios";

const EmailVerification = () => {
    const inputRef = useRef([]);
    const [loading, setLoading] = useState(false);
    const {getUserData, isLoggedIn, userData, backendURL} = useContext(AppContext);
    const navigate = useNavigate();

    // handleChange function 
    const handleChange = (e, index) => {
        const value = e.target.value.replace(/\D/,"")
        e.target.value  = value;

        if(value && index < 5){
            inputRef.current[index + 1].focus();
        }
    }

    const handleKeyDown = (e, index) => {
        if(e.key === "Backspace" && !e.target.value && index > 0){
            inputRef.current[index -1].focus();
        }
    }

    const handlePaste = (e) => {
        e.preventDefault();
        const paste = e.clipboardData.getData("text").slice(0,6).split("");
        paste.forEach((digit, i) =>{
            if(inputRef.current[i]){
                inputRef.current[i].value = digit;
            }
        });
        const next = paste.length < 6 ? paste.length : 5;
        inputRef.current[next].focus();
    }

    // for verifying email
    const handleVerify = async () => {
        const otp = inputRef.current.map(input => input.value).join("");
        if (otp.length !== 6) {
            toast.error("Please enter all 6 digits of the OTP.");
            return;
        }

        setLoading(true);
        try {
            const response = await axios.post(backendURL+ "/verify-otp", {otp});
            if (response.status === 200) {
                toast.success("OTP verified successfully!");
                getUserData();
                navigate("/");
            } else {
                toast.error("Invalid OTP");
            }
        }catch(error) {
            toast.error("Failed to verify OTP. Please try again.");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        isLoggedIn && userData && userData.isAccountVerified && navigate("/");
    }, [isLoggedIn, userData])

    return(
        <div className="email-verify-container d-flex align-items-center justify-content-start vh-100 position-relative"
            style={{background: "linear-gradient(90deg, #fffd78, #fdd017)", borderRadius: 0}}>
                <Link 
                    to="/" 
                    className="position-absolute top-0 start-0 p-4 d-flex align-items-center gap-2 text-decoration-none" 
                >
                    <img src={assets.logo_home} alt="logo" height={32} width={32} />
                    <span className="fs-4 fw-semibold text-dark">Kuya Guard</span>
                </Link>

                <div className="p-5 rounded-4 shadow" style={{position: "relative",  margin: "auto", maxWidth: "450px", width: "100%", backgroundColor: "rgba(211,211,211, 0.75)"}}>
                    <h4 className="text-center fw-bold mb-2">E-mail Verify OTP</h4>
                    
                    <p className="text-center mb-4">
                        I sent you an e-mail! Can you check it and type the 6-digit OTP here?
                    </p>

                    <div className="d-flex justify-content-between gap-2 mb-4 text-center text-white-50 mb-2">
                        {[...Array(6)].map((_, i) => (
                            <input 
                                key={i}
                                type="text"
                                maxLength={1}
                                className="form-control text-center fs-4 otp-input"
                                ref={(e) => (inputRef.current[i] = e)}
                                onChange={(e) => handleChange(e, i)}
                                onKeyDown={(e) => handleKeyDown(e, i)}
                                onPaste={handlePaste}
                            />
                        ))}
                    </div>

                    <button className="btn btn-primary w-100 fw-semibold" 
                        disabled={loading}
                        onClick={handleVerify}
                    >
                        {loading ? "Verifying..." : "Verify e-mail"}
                    </button>
                </div>

                
        </div>
    );
}

export default EmailVerification;