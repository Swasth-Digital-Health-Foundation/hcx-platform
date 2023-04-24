import React, { useState, useEffect } from 'react'
import { Segment, Tab } from 'semantic-ui-react'
import { UpdateRegistry } from './UpdateRegistry'
import { Grid, Image } from 'semantic-ui-react'
import { BasicDetails } from './BasicDetails'
import { useQuery } from '../service/QueryService'
import { SetPassword } from './SetPassword'
import { useHistory } from 'react-router-dom'

export const Onboarding = () => {

    let query = useQuery();
    const [activeIndex, setActiveIndex] = useState(0)
    const [state, setState] = useState({})
    let history = useHistory();
    const props = {
        changeTab: setActiveIndex,
        formState: state,
        setState
    }
    const panes = [
        { menuItem: 'Basic Details', render: () => <Tab.Pane><BasicDetails {...props} /></Tab.Pane> },
        { menuItem: 'Set or Verify Password', render: () => <Tab.Pane><SetPassword {...props} /></Tab.Pane> },
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
    
    function redirectHome(){
        history.push("/onboarding");
    }

    return <>
        <Grid centered container>
            <Grid.Row columns="1">
                <div className='banner' style={{ width: '65%', marginTop: '30px' }}>
                    <Grid.Column>
                        <Image src='/images/logo.png' onClick={e => {redirectHome()}} style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Segment style={{ width: '65%' }} textAlign='left'>
                <Tab
                    menu={{ fluid: true, vertical: true }}
                    menuPosition='left'
                    panes={panes}
                    activeIndex={activeIndex}
                />
            </Segment>
        </Grid>
    </>
}
