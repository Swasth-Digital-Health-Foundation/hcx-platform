import react from 'react';
import { Table } from 'react-bootstrap';
import { startCase } from 'lodash-es'
const RenderTable = ({ data = {} }) => {

    const getRows = data => {
        return data.map(([key, value], index) => {
            return <tr key={index}>
                <td>{startCase(key)}</td>
                <td>{value}</td>
            </tr>
        })
    }

    return <>
        <Table striped bordered hover>
            <thead>
                <tr>
                    <th>Key</th>
                    <th>Value</th>
                </tr>
            </thead>
            <tbody>
                {getRows(Object.entries(data))}
            </tbody>
        </Table>
    </>
}

export default RenderTable