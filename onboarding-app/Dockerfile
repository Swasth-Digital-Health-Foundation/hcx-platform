# Official Node.js runtime as a parent image
FROM node:19

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Install any needed packages
RUN npm install

# Make the app's build
RUN npm run build

# Expose port 3000 for the React app
EXPOSE 3000

# Start the app
CMD ["npm", "start"]