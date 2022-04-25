import react from 'react';
import { Button, Col, Container, Row } from 'react-bootstrap';
import ReactJson from 'react-json-view'
import { useHistory, useLocation } from 'react-router-dom';

const JsonViewer = (props) => {
    const location = useLocation();
    const history = useHistory();

    const state = location.state || {};

    return <>
        <Container>
            <Row>
                <Col className='m-3'>
                    <Button variant="primary" type="button" onClick={e => history.push({ pathname: '/eligibility-check' })}> Back  </Button>
                </Col>
            </Row>
            <Row>
                <Col className='m-3'>
                    {typeof state === 'string' && state}
                    {typeof state === 'object' && <ReactJson src={state} />}
                </Col>
            </Row>
        </Container>
    </>
}

export default JsonViewer