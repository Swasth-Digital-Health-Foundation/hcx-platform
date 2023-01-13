import React from 'react'
import { Segment, Tab } from 'semantic-ui-react'
import { OnBoarding } from './OnBoarding'
import { OTPVerify } from './OTPVerify'
import { UpdateRegistry } from './UpdateRegistry'
import { Grid, Image } from 'semantic-ui-react'

const panes = [
    { menuItem: 'Basic Details Submission', render: () => <Tab.Pane><OnBoarding /></Tab.Pane> },
    { menuItem: 'OTP Verification', render: () => <Tab.Pane><OTPVerify /></Tab.Pane> },
    { menuItem: 'Update Complete Details', render: () => <Tab.Pane><UpdateRegistry /></Tab.Pane> },
]

const TabExampleVerticalTabular = () => (
    <Grid centered container>
        <Grid.Row columns="1">
            <div className='banner'>
                <Grid.Column>
                    <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                </Grid.Column>
                <Grid.Column>
                    <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                </Grid.Column>
            </div>
        </Grid.Row>

        <Grid.Row centered container style={{ marginTop: '30px' }}>
            <Segment>
                <Tab
                    menu={{ fluid: true, vertical: true }}
                    menuPosition='left'
                    panes={panes}
                />
            </Segment>
        </Grid.Row>
    </Grid>

)

export default TabExampleVerticalTabular
