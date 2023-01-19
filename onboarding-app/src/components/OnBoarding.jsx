import React, { useState, useEffect } from 'react'
import { Segment, Tab } from 'semantic-ui-react'
import { OTPVerify } from './OTPVerify'
import { UpdateRegistry } from './UpdateRegistry'
import { Grid, Image } from 'semantic-ui-react'
import { BasicDetails } from './BasicDetails'
import { useQuery } from '../service/QueryService'

export const Onboarding = () => {

    let query = useQuery();
    const [activeIndex, setActiveIndex] = useState(0)
    const [state, setState] = useState({})
    const props = {
        changeTab: setActiveIndex,
        formState: state,
        setState
    }
    const panes = [
        { menuItem: 'Basic Details', render: () => <Tab.Pane><BasicDetails {...props} /></Tab.Pane> },
        { menuItem: 'OTP Verification', render: () => <Tab.Pane><OTPVerify {...props} /></Tab.Pane> },
        { menuItem: 'Update Complete Details', render: () => <Tab.Pane><UpdateRegistry {...props} /></Tab.Pane> },
    ]

    useEffect(() => {
        const formState = query.get("state");
        if (formState == null) {
            setActiveIndex(0);
        } else {
            setActiveIndex(formState);
        }
    }, []);

    return <>
        <Grid centered container>
            <Grid.Row columns="1">
                <div className='banner' style={{ width: '50%', marginTop: '30px' }}>
                    <Grid.Column>
                        <Image src='/images/logo.png' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Segment style={{ width: '50%' }} textAlign='left'>
            <Tab panes={panes} activeIndex={activeIndex}/>
                {/* <Tab
                    menu={{ textAlign: 'center', borderless: true, pointing: true, attached: false, tabular: false }}
                    panes={panes}
                /> */}
                {/* <Tab
                        menu={{ fluid: true, vertical: true }}
                        menuPosition='left'
                        panes={panes}
                        activeIndex={activeIndex}
                    /> */}
            </Segment>
        </Grid>
    </>
}
