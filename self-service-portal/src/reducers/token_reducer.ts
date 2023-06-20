import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'

export interface TokenInterface {
  participantToken: string
}

const initialState: TokenInterface = {
    participantToken: "abcd",
}

export const tokenReducer = createSlice({
  name: 'token',
  initialState,
  reducers: {
    addParticipantToken: (state, action:PayloadAction<string>) => {
      // Redux Toolkit allows us to write "mutating" logic in reducers. It
      // doesn't actually mutate the state because it uses the Immer library,
      // which detects changes to a "draft state" and produces a brand new
      // immutable state based off those changes
      state.participantToken = action.payload
    }
  },
})

// Action creators are generated for each case reducer function
export const { addParticipantToken } = tokenReducer.actions

export default tokenReducer.reducer