import { EligibilityCheck } from './components/check-eligibility';
import JsonViewer from './components/json-viewer/json-viewer';

export default [
    {
        "path": "/root",
        "exact": true,
        "component": EligibilityCheck,
        "children": [],
        "data": {}
    },
    {
        "path": "/json-viewer",
        "exact": true,
        "component": JsonViewer,
        "children": [],
        "data": {}
    }
]