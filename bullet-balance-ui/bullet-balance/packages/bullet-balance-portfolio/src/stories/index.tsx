import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { LoadingIndicatorComponent } from '../modules/loading-indicator/loading-indicator.component';

storiesOf('Loading indicator', module)
  .add('with text', () => (
    <LoadingIndicatorComponent/>
  ));   