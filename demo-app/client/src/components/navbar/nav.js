import react from 'react';
import { Navbar, Container } from 'react-bootstrap';
import img from '../../assets/hcx.png';
const Nav = props => {
    return <>
        <Navbar bg="dark" variant="dark">
            <Container>
                <Navbar.Brand href="/">
                    <img
                        alt=""
                        src={img}
                        height="40"
                        className="d-inline-block"
                    /> &nbsp; {'Swasth HCX POC Application'}
                </Navbar.Brand>
            </Container>
        </Navbar>
    </>
}

export default Nav;