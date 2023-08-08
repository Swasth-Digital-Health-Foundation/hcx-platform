/** @type {import('tailwindcss').Config} */

const _ = require('lodash');

module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}" ,"./node_modules/flowbite/**/*.js"],
  theme: {
    extend: {},
  },
  plugins: [
    function ({ addUtilities }) {
      [require('flowbite/plugin')]
      const gradients = {
        '.gradient-blue-purple': {
          background: 'linear-gradient(to right, #1C4DC3, #3632BE, #1D1991, #060347)',
        },
        // Add more gradients here as needed
      };

      addUtilities(gradients, {
        variants: ['responsive'],
      });
    },
  ],
  darkMode: "class"
};
