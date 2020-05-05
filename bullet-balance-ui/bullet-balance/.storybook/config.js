// https://github.com/serhii-havrylenko/monorepo-babel-ts-lerna-starter/blob/master/.storybook/config.js
import { configure } from '@storybook/react';

function loadStories() {
  const stories = require("../packages/bullet-balance-portfolio/src/stories/index");
}

configure(loadStories, module);