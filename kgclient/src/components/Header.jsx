import { useContext } from "react";
import { assets } from "../assets/assets";
import { AppContext } from "../context/AppContext";
import {useNavigate} from "react-router-dom";

const Header = () => {
    const {userData} = useContext(AppContext);
    const navigate = useNavigate();

    return(
        <div className="text-center d-flex flex-column align-items-center justify-content-center py-5 px-3" 
            style={{ minHeight: "80vh" }}>
            <img src={assets.header02} alt="header" width={200} className="mb-4" />

            <h5 className="fw-semibold">
                Magandang araw {userData ? userData.name : "sa iyo"}! <span role="img" aria-label="wave">🫡</span>
            </h5>

            <h1 className="fw-bold display-5 mb-3">Welcome to Kuya Guard!</h1>

            <p className="text-muted fs-5 mb-4" style={{maxWidth: "500px"}}>
                I'll make sure you are always secured every time you register, authenticate and log in. <br></br>Explore me now!
            </p>

            <button className="btn btn-outline-dark rounded-pill px-4 py-2" onClick={() => navigate("/login")}>
                Let's get started!
            </button>
        </div>
    );
}

export default Header;