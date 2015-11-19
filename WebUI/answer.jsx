import React,{Component} from 'react';

export default class AnswerUI extends Component {

	render() {
		return(<div className="answerModule">{this.props.answer}</div>);
	}

}