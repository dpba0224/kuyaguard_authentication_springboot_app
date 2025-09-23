import Menubar from "../components/Menubar"
import Header from "../components/Header"

const Home = () => {
    return(
        <div className="flex flex-col items-center justify-content-center min-vh-100"
            style={{ background: "linear-gradient(90deg, #89ecda, #3bd6c6)" }}>
            <Menubar />
            <Header />
        </div>
    );
}

export default Home;