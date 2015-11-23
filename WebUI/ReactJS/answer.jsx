import React,{Component} from 'react';

export default class AnswerUI extends Component {

	render() {
		if ( this.props.answer === '' ) {
			return(<div></div>);
		}

		let facts = this.props.answer.inferedFacts.map((item) => {

			let xx;
			for ( key in item.data ) {
				if ( item.data.hasOwnProperty(key) ) {
					xx += <div>{item.data[key]}</div>
				}
			}
			return (
				<div className="answerContainer">{xx}</div>
			);
		})

		return(
			<div>
				<div className="answerContainer">{this.props.answer.text}</div>
				<div>{facts}</div>
			</div>
		);
	}

}