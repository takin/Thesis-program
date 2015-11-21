import React,{Component} from 'react';

export default class AnswerUI extends Component {

	render() {
		if ( this.props.answer === '' ) {
			return(<div></div>);
		}
		return(<div id="answerContainer">{this.props.answer.toString()}</div>);
	}

}