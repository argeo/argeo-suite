const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');

module.exports = {
	mode: 'development',
	devtool: 'source-map', // original code
	//mode: 'production',
	entry: {
		index: './src/org.argeo.app.geo.js/index.js'
	},
	output: {
		filename: '[name].[contenthash].js',
		path: path.resolve(__dirname, 'org/argeo/app/geo/js'),
		clean: true,
	},
	optimization: {
		moduleIds: 'deterministic',
		runtimeChunk: 'single',
		// split code
		splitChunks: {
			chunks: 'all',
		},
	},
	module: {
		rules: [
			{
				test: /\.(css)$/,
				use: [
					MiniCssExtractPlugin.loader,
					'css-loader',
				],
			},
		],
	},
	plugins: [
		// deal with CSS
		new MiniCssExtractPlugin(),
		// deal with HTML generation
		new HtmlWebpackPlugin({
			title: 'Open Layers',
			template: 'src/org.argeo.app.geo.js/index.html',
		}),

	],
};