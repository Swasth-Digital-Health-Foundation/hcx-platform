import { assign, createMachine } from 'xstate';
import axios from 'axios'
import messages from '../utils/message.json';
import { get as _get, find as _find } from 'lodash-es';

const machine = createMachine({
    id: 'checkEligibility',
    initial: 'initial',
    context: {
        event: null,
        acknowledgement: null,
        message: '',
        hcxResponse: null,
        error: null,
        timeTakenForResponse: 0,
        request: null
    },
    states: {
        initial: {
            on: {
                SUBMIT: {
                    target: 'loading',
                    actions: assign({
                        event: (_, event) => {
                            return event;
                        },
                        message: messages.REQUEST_SUBMITTING,
                        timeTakenForResponse: () => Date.now()
                    })
                }
            }
        },
        loading: {
            invoke: {
                id: "check-eligilibity",
                src: "service",
                onDone: {
                    target: "acknowledged",
                    actions: assign({
                        acknowledgement: (_, event) => event?.data?.acknowledgement,
                        request: (_, event) => event?.data?.request,
                        message: messages.REQUEST_SUBMITTED
                    })
                },
                onError: {
                    target: 'rejected',
                    actions: assign({
                        message: (_, event) => event.data?.response?.data?.message,
                        error: (_, event) => {
                            return event?.data?.response?.data;
                        },
                        timeTakenForResponse: (payload, _) => {
                            return Date.now() - payload.timeTakenForResponse;
                        }
                    })
                }
            },
        },
        acknowledged: {
            on: {
                ACKNOWLEDGEMENT_SUCCESS: {
                    target: 'resolved',
                    actions: assign({
                        message: (context, event) => {
                            const hcxResponse = event?.payload;

                            if (hcxResponse && 'x-hcx-error_details' in hcxResponse) {
                                return messages.SOMETHING_WENT_WRONG
                            }

                            const { event: { requestType } } = context;
                            const { entry = [] } = hcxResponse;
                            switch (requestType) {
                                case 'ELIGIBILITY': {
                                    return messages.ELIGIBLE_SUCCESS
                                }

                                case 'CLAIM': {
                                    const claimResponse = _find(entry, e => e?.resource?.resourceType === 'ClaimResponse');
                                    const amount = _get(claimResponse, 'resource.item[0].adjudication[0].amount.value');
                                    return messages.CLAIM_SUCCESS.replace('xxx', amount ?? 0)
                                }

                                case 'PREAUTH': {
                                    const preAuthResponse = _find(entry, e => e?.resource?.resourceType === 'PreauthResponse');
                                    const amount = _get(preAuthResponse, 'resource.item[0].adjudication[0].amount.value');
                                    return messages.PRE_AUTH_SUCCESS.replace('xxx', amount ?? 0);
                                }
                            }
                        },
                        hcxResponse: (_, event) => event?.payload,
                        timeTakenForResponse: (payload, _) => {
                            return Date.now() - payload.timeTakenForResponse;
                        }
                    })
                }
            },
            after: {
                200000: {
                    target: 'rejected',
                    actions: assign({
                        acknowledgement: (_, event) => null,
                        message: messages.ELIGIBLE_FAILURE,
                        error: () => ({ error: 'Timeout' }),
                        timeTakenForResponse: (payload, _) => {
                            return Date.now() - payload.timeTakenForResponse;
                        }
                    })
                }
            }
        },
        resolved: {
            on: {
                RETRY: {
                    target: 'initial'
                }
            }

        },
        rejected: {
            on: {
                RETRY: {
                    target: 'initial'
                }
            }
        }
    }
}, {
    services: {
        service: (context, event) => {
            const { payload, requestType } = event;
            switch (requestType) {
                case 'ELIGIBILITY': {
                    return axios.post('http://localhost:8000/v1/coverageeligibility/check', payload)
                        .then(response => response.data);
                }

                case 'CLAIM': {
                    return axios.post('http://localhost:8000/v1/claim/submit', payload)
                        .then(response => response.data);
                }

                case 'PREAUTH': {
                    return axios.post('http://localhost:8000/v1/preauth/submit', payload)
                        .then(response => response.data);
                }
            }
        }
    }
});

export { machine }