import * as React from 'react';
import { Optional } from "../../model/optional.model";

export type NullableComponentProps = {
    value: Optional<any>;
}

export const NullableComponent = (props: NullableComponentProps) => {
    return <span>{props.value.getOrElse("-")}</span>
}