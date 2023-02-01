import { configureStore, createSlice } from '@reduxjs/toolkit'

const initialState = {}

export const formSlice = createSlice({
    name: 'formState',
    initialState,
    reducers: {
      updateForm: (state, action) => {
        return {
            ...state, 
            ...action.payload
        }
      }
    },
  })

export const { updateForm } = formSlice.actions
export default formSlice.reducer

export const store = configureStore({
    reducer: {
      formState: formSlice.reducer,
    },
  })