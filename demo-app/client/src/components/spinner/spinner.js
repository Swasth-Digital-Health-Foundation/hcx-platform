import react from 'react';
import { Button, Spinner } from 'react-bootstrap';

const Loader = ({ message = 'Loading ...' }) => {
    return <Button variant="primary" disabled>
        <Spinner
            as="div"
            animation="grow"
            size="sm"
            role="status"
            aria-hidden="true"
        />
         &nbsp; {message}
    </Button>
}

export default Loader;