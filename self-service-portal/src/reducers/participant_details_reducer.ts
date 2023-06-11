import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'

export interface ParticipantDetaislInterface {
  participantDetails: Object
}

const initialState: ParticipantDetaislInterface = {
    participantDetails: {},
}

export const participantDetailsReducer = createSlice({
  name: 'loginParticipantDetails',
  initialState,
  reducers: {
    addParticipantDetails: (state, action: PayloadAction<Object>) => {
      // Redux Toolkit allows us to write "mutating" logic in reducers. It
      // doesn't actually mutate the state because it uses the Immer library,
      // which detects changes to a "draft state" and produces a brand new
      // immutable state based off those changes
      state.participantDetails = {...action.payload}
    }
  },
})

// Action creators are generated for each case reducer function
export const { addParticipantDetails } = participantDetailsReducer.actions

export default participantDetailsReducer.reducer