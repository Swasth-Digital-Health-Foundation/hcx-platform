import react from 'react';
import { Alert } from 'react-bootstrap';
import ReactJson from 'react-json-view'

const Info = ({ heading = '', body, footer = {}, type = 'danger', showFooter = false }) => {

    if (type === 'success' && footer && typeof (footer) === 'object' && ('error' in footer)) {
        type = 'danger';
        heading = 'Error !!'
    }

    return <>
        <Alert variant={type}>
            <Alert.Heading>{heading}</Alert.Heading>
            <p>
                {body || (type === 'danger' ? 'Something went wrong. Please try again later ...' : '')}
            </p>
            <hr />
            {footer && showFooter &&
                <div className="mb-0">
                    <hr />
                    {typeof footer === 'string' && footer}
                    {typeof footer === 'object' && <ReactJson src={footer} />}
                </div>
            }
        </Alert>
    </>
}

export default Info